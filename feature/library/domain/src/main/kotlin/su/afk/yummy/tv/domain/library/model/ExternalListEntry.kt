package su.afk.yummy.tv.domain.library.model

/**
 * Элемент списка аниме из внешнего источника или для экспорта.
 *
 * @param externalId идентификатор во внешней базе данных (MAL ID или Shikimori ID).
 * @param title название тайтла.
 * @param status статус просмотра.
 * @param episodesWatched количество просмотренных серий.
 * @param score пользовательская оценка (1–10) или null, если оценка не выставлена.
 */
data class ExternalListEntry(
    val externalId: Int,
    val title: String,
    val status: ExternalAnimeStatus,
    val episodesWatched: Int = 0,
    val score: Int? = null,
)
