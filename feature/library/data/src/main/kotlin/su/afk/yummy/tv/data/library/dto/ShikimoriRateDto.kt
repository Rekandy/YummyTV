package su.afk.yummy.tv.data.library.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** DTO элемента списка аниме из JSON-экспорта Shikimori. */
@Serializable
data class ShikimoriRateDto(
    val id: Long? = null,
    @SerialName("target_id") val targetId: Int? = null,
    @SerialName("anime_id") val animeId: Int? = null,
    @SerialName("target_title") val targetTitle: String? = null,
    @SerialName("target_title_ru") val targetTitleRu: String? = null,
    @SerialName("target_type") val targetType: String? = null,
    val score: Int = 0,
    val status: String = "",
    val episodes: Int = 0,
    val text: String? = null,
    val anime: ShikimoriAnimeDto? = null,
)

/** DTO информации об аниме внутри пользовательского рейтинга Shikimori. */
@Serializable
data class ShikimoriAnimeDto(
    val id: Int? = null,
    val name: String? = null,
    @SerialName("russian") val nameRu: String? = null,
)
