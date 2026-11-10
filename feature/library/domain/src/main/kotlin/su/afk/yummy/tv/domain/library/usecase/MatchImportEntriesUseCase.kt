package su.afk.yummy.tv.domain.library.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import su.afk.yummy.tv.domain.library.model.ExternalListEntry
import su.afk.yummy.tv.domain.library.model.LibraryItem
import su.afk.yummy.tv.domain.library.model.SyncPreviewItem
import su.afk.yummy.tv.domain.library.model.toListId
import su.afk.yummy.tv.domain.library.repository.LibraryRepository
import su.afk.yummy.tv.domain.library.repository.ListSyncRepository
import javax.inject.Inject

/** Сопоставляет записи из внешнего файла со списком локальной библиотеки пользователя и каталогом YummyTV. */
class MatchImportEntriesUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val listSyncRepository: ListSyncRepository,
) {
    suspend operator fun invoke(entries: List<ExternalListEntry>): List<SyncPreviewItem> = coroutineScope {
        val localItems = libraryRepository.getAll()
        val localByNormalizedTitle = localItems.associateBy { normalize(it.title) }
        val localById = localItems.associateBy { it.animeId }

        // Устранение дубликатов из исходного файла
        val distinctEntries = entries.distinctBy { it.externalId to normalize(it.title) }

        // Безопасное сопоставление небольшими пачками по 3 запроса с защитой от rate-limit
        distinctEntries.chunked(3).flatMap { chunk ->
            val result = chunk.map { entry ->
                async {
                    matchEntry(entry, localById, localByNormalizedTitle)
                }
            }.awaitAll()
            kotlinx.coroutines.delay(100) // вежливая пауза между пачками
            result
        }
    }

    private suspend fun matchEntry(
        entry: ExternalListEntry,
        localById: Map<Int, LibraryItem>,
        localByNormalizedTitle: Map<String, LibraryItem>,
    ): SyncPreviewItem {
        // 1. Проверяем локальную библиотеку пользователя по нормализованному названию
        var matchedLocal = localByNormalizedTitle[normalize(entry.title)]

        // 2. Если в локальной библиотеке не найден, разрешаем тайтл в каталоге YummyTV (Yani)
        val resolved = if (matchedLocal == null) {
            listSyncRepository.resolveYaniAnime(entry)
        } else {
            null
        }

        // Если тайтл разрешился в каталоге Yani, проверяем — вдруг он есть в библиотеке по своему реальному animeId
        if (resolved != null && matchedLocal == null) {
            matchedLocal = localById[resolved.animeId]
                ?: localByNormalizedTitle[normalize(resolved.title)]
        }

        val effectiveLocalId = matchedLocal?.animeId ?: resolved?.animeId
        val effectiveTitle = matchedLocal?.title ?: resolved?.title

        val hasConflict = if (matchedLocal != null) {
            detectConflict(entry, matchedLocal)
        } else {
            false
        }

        return SyncPreviewItem(
            entry = entry,
            localAnimeId = effectiveLocalId,
            localTitle = effectiveTitle,
            hasConflict = hasConflict,
            resolvedAnime = resolved,
        )
    }

    private fun detectConflict(entry: ExternalListEntry, local: LibraryItem): Boolean {
        val targetListId = entry.status.toListId()
        val statusChanged = local.listId != targetListId
        val ratingChanged = entry.score != null && entry.score != local.userRating
        return statusChanged || ratingChanged
    }

    private fun normalize(text: String): String =
        text.lowercase().replace(Regex("[^a-zа-я0-9]"), "").trim()
}
