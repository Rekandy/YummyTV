package su.afk.yummy.tv.data.library.mapper

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import su.afk.yummy.tv.data.library.dto.ShikimoriAnimeDto
import su.afk.yummy.tv.data.library.dto.ShikimoriExportDto
import su.afk.yummy.tv.data.library.dto.ShikimoriRateDto
import su.afk.yummy.tv.domain.library.model.ExternalAnimeStatus
import su.afk.yummy.tv.domain.library.model.ExternalListEntry
import su.afk.yummy.tv.domain.library.model.ExternalService
import su.afk.yummy.tv.domain.library.model.ParsedImportFile

private val listSyncJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    prettyPrint = true
}

/** Разбирает JSON-экспорт Shikimori в [ParsedImportFile]. */
internal fun parseShikimoriJson(text: String): ParsedImportFile {
    val rates = listSyncJson.decodeFromString<List<ShikimoriRateDto>>(text)
    val entries = rates.mapNotNull { rate ->
        if (rate.targetType != null && !rate.targetType.equals("Anime", ignoreCase = true)) {
            return@mapNotNull null
        }

        val animeId = rate.targetId ?: rate.anime?.id ?: rate.animeId ?: 0
        val title = rate.targetTitleRu?.takeIf { it.isNotBlank() }
            ?: rate.targetTitle?.takeIf { it.isNotBlank() }
            ?: rate.anime?.nameRu?.takeIf { it.isNotBlank() }
            ?: rate.anime?.name?.takeIf { it.isNotBlank() }
            ?: rate.text?.takeIf { it.isNotBlank() }
            ?: if (animeId > 0) "Anime #$animeId" else return@mapNotNull null

        val status = rate.status.toExternalAnimeStatusFromShikimori()
            ?: ExternalAnimeStatus.PLAN_TO_WATCH

        ExternalListEntry(
            externalId = animeId,
            title = title,
            status = status,
            episodesWatched = rate.episodes,
            score = if (rate.score in 1..10) rate.score else null,
        )
    }
    return ParsedImportFile(
        service = ExternalService.SHIKIMORI,
        entries = entries,
    )
}

/** Формирует JSON-документ экспорта для Shikimori. */
internal fun buildShikimoriJson(entries: List<ExternalListEntry>): String {
    val exportItems = entries.map { entry ->
        ShikimoriExportDto(
            targetId = entry.externalId,
            targetType = "Anime",
            score = entry.score ?: 0,
            status = entry.status.toShikimoriStatusString(),
            episodes = entry.episodesWatched,
            anime = ShikimoriAnimeDto(
                id = entry.externalId,
                name = entry.title,
                nameRu = entry.title,
            ),
        )
    }
    return listSyncJson.encodeToString(exportItems)
}

/** Разбирает строковый статус из Shikimori JSON в [ExternalAnimeStatus]. */
internal fun String.toExternalAnimeStatusFromShikimori(): ExternalAnimeStatus? = when (trim().lowercase()) {
    "watching", "rewatching", "смотрю", "пересматриваю" -> ExternalAnimeStatus.WATCHING
    "completed", "просмотрено", "просмотрен" -> ExternalAnimeStatus.COMPLETED
    "on_hold", "on-hold", "отложено" -> ExternalAnimeStatus.ON_HOLD
    "dropped", "брошено" -> ExternalAnimeStatus.DROPPED
    "planned", "plan_to_watch", "в планах", "запланировано" -> ExternalAnimeStatus.PLAN_TO_WATCH
    else -> null
}

/** Возвращает строковый статус для Shikimori JSON экспорта. */
internal fun ExternalAnimeStatus.toShikimoriStatusString(): String = when (this) {
    ExternalAnimeStatus.WATCHING -> "watching"
    ExternalAnimeStatus.COMPLETED -> "completed"
    ExternalAnimeStatus.ON_HOLD -> "on_hold"
    ExternalAnimeStatus.DROPPED -> "dropped"
    ExternalAnimeStatus.PLAN_TO_WATCH -> "planned"
}
