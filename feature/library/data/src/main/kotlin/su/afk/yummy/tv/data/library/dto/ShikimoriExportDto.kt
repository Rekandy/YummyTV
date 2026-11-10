package su.afk.yummy.tv.data.library.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** DTO для экспорта списка аниме в формат Shikimori JSON. */
@Serializable
data class ShikimoriExportDto(
    @SerialName("target_id") val targetId: Int,
    @SerialName("target_type") val targetType: String = "Anime",
    @SerialName("target_title") val targetTitle: String? = null,
    @SerialName("target_title_ru") val targetTitleRu: String? = null,
    val score: Int = 0,
    val status: String,
    val episodes: Int = 0,
    val text: String? = null,
    val anime: ShikimoriAnimeDto? = null,
)
