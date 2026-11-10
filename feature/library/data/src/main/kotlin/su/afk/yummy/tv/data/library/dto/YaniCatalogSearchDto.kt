package su.afk.yummy.tv.data.library.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class YaniCatalogSearchResponseDto(
    val response: List<YaniCatalogSearchItemDto> = emptyList(),
)

@Serializable
internal data class YaniCatalogSearchItemDto(
    @SerialName("anime_id") val animeId: Int? = null,
    val title: String = "",
    val poster: YaniCatalogSearchPosterDto? = null,
    val rating: YaniCatalogSearchRatingDto? = null,
    val year: Int? = null,
    @SerialName("remote_ids") val remoteIds: YaniCatalogSearchRemoteIdsDto? = null,
)

@Serializable
internal data class YaniCatalogSearchPosterDto(
    val small: String? = null,
    val medium: String? = null,
    val big: String? = null,
    val fullsize: String? = null,
    val mega: String? = null,
)

@Serializable
internal data class YaniCatalogSearchRatingDto(
    val average: Double? = null,
)

@Serializable
internal data class YaniCatalogSearchRemoteIdsDto(
    @SerialName("shikimori_id") val shikimoriId: Int? = null,
    @SerialName("myanimelist_id") val myAnimeListId: Int? = null,
)

@Serializable
internal data class YaniRemoteIdsDetailsResponseDto(
    val response: YaniRemoteIdsWrapperDto = YaniRemoteIdsWrapperDto(),
)

@Serializable
internal data class YaniRemoteIdsWrapperDto(
    @SerialName("remote_ids") val remoteIds: YaniCatalogSearchRemoteIdsDto? = null,
)
