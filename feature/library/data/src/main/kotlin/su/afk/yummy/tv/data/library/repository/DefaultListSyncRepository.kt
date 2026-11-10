package su.afk.yummy.tv.data.library.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import su.afk.yummy.tv.core.network.yani.YANI_BASE_URL
import su.afk.yummy.tv.core.network.yani.YaniHttpClientProvider
import su.afk.yummy.tv.data.library.dto.YaniCatalogSearchResponseDto
import su.afk.yummy.tv.data.library.dto.YaniRemoteIdsDetailsResponseDto
import su.afk.yummy.tv.data.library.mapper.buildMalXml
import su.afk.yummy.tv.data.library.mapper.buildShikimoriJson
import su.afk.yummy.tv.data.library.mapper.normalizeCatalogTitle
import su.afk.yummy.tv.data.library.mapper.parseMalXml
import su.afk.yummy.tv.data.library.mapper.parseShikimoriJson
import su.afk.yummy.tv.data.library.mapper.toResolvedRef
import su.afk.yummy.tv.domain.library.model.ExternalListEntry
import su.afk.yummy.tv.domain.library.model.ExternalService
import su.afk.yummy.tv.domain.library.model.ParsedImportFile
import su.afk.yummy.tv.domain.library.model.ResolvedAnimeRef
import su.afk.yummy.tv.domain.library.repository.ListSyncRepository
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class DefaultListSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clientProvider: YaniHttpClientProvider,
) : ListSyncRepository {

    private val resolveCache = ConcurrentHashMap<String, ResolvedAnimeRef>()
    private val malIdCache = ConcurrentHashMap<Int, Int>()
    private val networkRateLimiter = Semaphore(2)

    override suspend fun parseImportFile(uri: String): ParsedImportFile = withContext(Dispatchers.IO) {
        val contentUri = Uri.parse(uri)
        val stream = context.contentResolver.openInputStream(contentUri)
            ?: error("Не удалось открыть файл: $uri")

        stream.use { input ->
            val content = input.bufferedReader(Charsets.UTF_8).readText().trim()
            if (content.startsWith("<?xml") || content.contains("<myanimelist")) {
                parseMalXml(content.byteInputStream(Charsets.UTF_8))
            } else {
                parseShikimoriJson(content)
            }
        }
    }

    override suspend fun buildExportFile(
        entries: List<ExternalListEntry>,
        service: ExternalService,
        username: String,
    ): String = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "logs/export").apply { mkdirs() }
        val (fileName, fileContent) = when (service) {
            ExternalService.MAL -> "yummytv_mal_export.xml" to buildMalXml(entries, username)
            ExternalService.SHIKIMORI -> "yummytv_shikimori_export.json" to buildShikimoriJson(entries)
        }
        val exportFile = File(exportDir, fileName)
        exportFile.writeText(fileContent, Charsets.UTF_8)
        exportFile.absolutePath
    }

    override suspend fun resolveYaniAnime(entry: ExternalListEntry): ResolvedAnimeRef? = withContext(Dispatchers.IO) {
        val cacheKey = "${entry.externalId}:${entry.title}"
        resolveCache[cacheKey]?.let { return@withContext it }

        val query = entry.title.trim()
        if (query.isBlank()) return@withContext null

        networkRateLimiter.withPermit {
            resolveCache[cacheKey]?.let { return@withPermit it }

            try {
                delay(80)

                val response: YaniCatalogSearchResponseDto = clientProvider.get().get("$YANI_BASE_URL/anime") {
                    parameter("q", query)
                    parameter("limit", 10)
                }.body()

                val items = response.response
                if (items.isEmpty()) return@withPermit null

                // 1. Точное совпадение по externalId (MAL / Shikimori ID) в remote_ids
                val idMatch = items.firstOrNull { item ->
                    item.animeId != null && (
                        item.remoteIds?.shikimoriId == entry.externalId ||
                        item.remoteIds?.myAnimeListId == entry.externalId
                    )
                }
                if (idMatch != null) {
                    val ref = idMatch.toResolvedRef()
                    resolveCache[cacheKey] = ref
                    return@withPermit ref
                }

                // 2. Точное совпадение по нормализованному названию
                val normEntry = normalizeCatalogTitle(entry.title)
                val titleMatch = items.firstOrNull { item ->
                    item.animeId != null && normalizeCatalogTitle(item.title) == normEntry
                }
                if (titleMatch != null) {
                    val ref = titleMatch.toResolvedRef()
                    resolveCache[cacheKey] = ref
                    return@withPermit ref
                }

                // 3. Совпадение по подстроке
                val partialMatch = items.firstOrNull { item ->
                    if (item.animeId == null) return@firstOrNull false
                    val normItem = normalizeCatalogTitle(item.title)
                    normItem.contains(normEntry) || normEntry.contains(normItem)
                }
                if (partialMatch != null) {
                    val ref = partialMatch.toResolvedRef()
                    resolveCache[cacheKey] = ref
                    return@withPermit ref
                }

                // 4. Первый результат поиска как fallback
                val fallback = items.firstOrNull { it.animeId != null }?.toResolvedRef()
                if (fallback != null) {
                    resolveCache[cacheKey] = fallback
                }
                fallback
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun getMalIdForAnime(animeId: Int): Int? = withContext(Dispatchers.IO) {
        malIdCache[animeId]?.let { return@withContext it }

        try {
            val response: YaniRemoteIdsDetailsResponseDto = clientProvider.get()
                .get("$YANI_BASE_URL/anime/$animeId")
                .body()
            val malId = response.response.remoteIds?.myAnimeListId
                ?: response.response.remoteIds?.shikimoriId
            if (malId != null && malId > 0) {
                malIdCache[animeId] = malId
            }
            malId
        } catch (e: Exception) {
            null
        }
    }
}
