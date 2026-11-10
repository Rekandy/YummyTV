package su.afk.yummy.tv.domain.library.model

/**
 * Элемент предварительного просмотра перед импортом.
 *
 * @param entry запись из импортируемого файла.
 * @param localAnimeId id тайтла в библиотеке YummyTV, если он найден; null — тайтл не найден.
 * @param localTitle название тайтла в локальной библиотеке.
 * @param hasConflict true, если тайтл уже присутствует в библиотеке с отличающимся статусом или оценкой.
 * @param resolvedAnime найденные данные тайтла из каталога YummyTV (постер, рейтинг, год).
 */
data class SyncPreviewItem(
    val entry: ExternalListEntry,
    val localAnimeId: Int?,
    val localTitle: String?,
    val hasConflict: Boolean,
    val resolvedAnime: ResolvedAnimeRef? = null,
)
