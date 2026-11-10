package su.afk.yummy.tv.domain.library.model

import su.afk.yummy.tv.core.model.anime.AnimePoster

/**
 * Данные найденного тайтла в каталоге YummyTV (Yani).
 *
 * @param animeId реальный внутренний ID тайтла в YummyTV.
 * @param title русское название тайтла в каталоге YummyTV.
 * @param poster постер тайтла.
 * @param rating средний рейтинг тайтла.
 * @param year год выхода тайтла.
 */
data class ResolvedAnimeRef(
    val animeId: Int,
    val title: String,
    val poster: AnimePoster? = null,
    val rating: Double? = null,
    val year: Int? = null,
)
