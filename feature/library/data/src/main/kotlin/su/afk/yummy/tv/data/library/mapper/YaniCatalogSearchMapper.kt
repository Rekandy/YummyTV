package su.afk.yummy.tv.data.library.mapper

import su.afk.yummy.tv.core.model.anime.AnimePoster
import su.afk.yummy.tv.data.library.dto.YaniCatalogSearchItemDto
import su.afk.yummy.tv.domain.library.model.ResolvedAnimeRef

/** Преобразует элемент ответа каталога Yani в ссылку на найденное аниме [ResolvedAnimeRef]. */
internal fun YaniCatalogSearchItemDto.toResolvedRef(): ResolvedAnimeRef {
    val animePoster = poster?.let { p ->
        AnimePoster(
            small = p.small,
            medium = p.medium,
            big = p.big,
            fullsize = p.fullsize,
            mega = p.mega,
        )
    }
    return ResolvedAnimeRef(
        animeId = animeId ?: 0,
        title = title,
        poster = animePoster,
        rating = rating?.average,
        year = year,
    )
}

/** Нормализует название тайтла для устойчивого сопоставления. */
internal fun normalizeCatalogTitle(text: String): String =
    text.lowercase().replace(Regex("[^a-zа-я0-9]"), "").trim()
