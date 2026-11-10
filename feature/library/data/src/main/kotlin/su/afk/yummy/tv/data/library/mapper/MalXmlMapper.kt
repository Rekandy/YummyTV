package su.afk.yummy.tv.data.library.mapper

import org.w3c.dom.Element
import org.w3c.dom.Node
import su.afk.yummy.tv.domain.library.model.ExternalAnimeStatus
import su.afk.yummy.tv.domain.library.model.ExternalListEntry
import su.afk.yummy.tv.domain.library.model.ExternalService
import su.afk.yummy.tv.domain.library.model.ParsedImportFile
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

/** Разбирает XML-экспорт MyAnimeList в [ParsedImportFile]. */
internal fun parseMalXml(stream: InputStream): ParsedImportFile {
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val document = builder.parse(stream)
    document.documentElement.normalize()

    var username: String? = null
    val myInfoList = document.getElementsByTagName("myinfo")
    if (myInfoList.length > 0) {
        val myInfo = myInfoList.item(0) as? Element
        username = myInfo?.getElementsByTagName("user_name")?.item(0)?.textContent?.trim()
    }

    val entries = mutableListOf<ExternalListEntry>()
    val animeNodes = document.getElementsByTagName("anime")
    for (i in 0 until animeNodes.length) {
        val node = animeNodes.item(i)
        if (node.nodeType == Node.ELEMENT_NODE) {
            val element = node as Element
            val animeId = element.getElementsByTagName("series_animedb_id").item(0)?.textContent?.trim()?.toIntOrNull() ?: 0
            val title = element.getElementsByTagName("series_title").item(0)?.textContent?.trim().orEmpty()
            val episodesWatched = element.getElementsByTagName("my_watched_episodes").item(0)?.textContent?.trim()?.toIntOrNull() ?: 0
            val scoreVal = element.getElementsByTagName("my_score").item(0)?.textContent?.trim()?.toIntOrNull() ?: 0
            val statusText = element.getElementsByTagName("my_status").item(0)?.textContent?.trim().orEmpty()

            if (animeId > 0 || title.isNotBlank()) {
                val status = statusText.toExternalAnimeStatusFromMal() ?: ExternalAnimeStatus.PLAN_TO_WATCH
                entries += ExternalListEntry(
                    externalId = animeId,
                    title = title,
                    status = status,
                    episodesWatched = episodesWatched,
                    score = if (scoreVal in 1..10) scoreVal else null,
                )
            }
        }
    }

    return ParsedImportFile(
        service = ExternalService.MAL,
        entries = entries,
        username = username,
    )
}

/** Формирует XML-документ экспорта для MyAnimeList. */
internal fun buildMalXml(entries: List<ExternalListEntry>, username: String): String {
    val sb = StringBuilder()
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n")
    sb.append("<myanimelist>\n")
    sb.append("  <myinfo>\n")
    sb.append("    <user_name>").append(escapeXml(username)).append("</user_name>\n")
    sb.append("    <export_type>1</export_type>\n")
    sb.append("  </myinfo>\n")

    for (entry in entries) {
        sb.append("  <anime>\n")
        sb.append("    <series_animedb_id>").append(entry.externalId).append("</series_animedb_id>\n")
        sb.append("    <series_title><![CDATA[").append(entry.title).append("]]></series_title>\n")
        sb.append("    <my_watched_episodes>").append(entry.episodesWatched).append("</my_watched_episodes>\n")
        sb.append("    <my_score>").append(entry.score ?: 0).append("</my_score>\n")
        sb.append("    <my_status>").append(entry.status.toMalStatusString()).append("</my_status>\n")
        sb.append("  </anime>\n")
    }

    sb.append("</myanimelist>\n")
    return sb.toString()
}

/** Разбирает текстовый статус из MyAnimeList XML в [ExternalAnimeStatus]. */
internal fun String.toExternalAnimeStatusFromMal(): ExternalAnimeStatus? = when (trim().lowercase()) {
    "watching", "currently watching", "currently_watching" -> ExternalAnimeStatus.WATCHING
    "completed" -> ExternalAnimeStatus.COMPLETED
    "on-hold", "onhold", "on_hold" -> ExternalAnimeStatus.ON_HOLD
    "dropped" -> ExternalAnimeStatus.DROPPED
    "plan to watch", "plantowatch", "plan_to_watch" -> ExternalAnimeStatus.PLAN_TO_WATCH
    else -> null
}

/** Возвращает название статуса для MyAnimeList XML экспорта. */
internal fun ExternalAnimeStatus.toMalStatusString(): String = when (this) {
    ExternalAnimeStatus.WATCHING -> "Watching"
    ExternalAnimeStatus.COMPLETED -> "Completed"
    ExternalAnimeStatus.ON_HOLD -> "On-Hold"
    ExternalAnimeStatus.DROPPED -> "Dropped"
    ExternalAnimeStatus.PLAN_TO_WATCH -> "Plan to Watch"
}

private fun escapeXml(str: String): String =
    str.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
