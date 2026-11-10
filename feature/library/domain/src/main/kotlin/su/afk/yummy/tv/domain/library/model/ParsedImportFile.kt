package su.afk.yummy.tv.domain.library.model

/**
 * Разобранные данные из файла импорта списков.
 *
 * @param service сервис, формат которого обнаружен (MAL или Shikimori).
 * @param entries список извлечённых записей.
 * @param username имя пользователя из файла, если указано.
 */
data class ParsedImportFile(
    val service: ExternalService,
    val entries: List<ExternalListEntry>,
    val username: String? = null,
)
