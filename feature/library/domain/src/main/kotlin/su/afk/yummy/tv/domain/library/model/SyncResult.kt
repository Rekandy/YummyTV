package su.afk.yummy.tv.domain.library.model

/**
 * Итоговый результат операции синхронизации.
 *
 * @param total общее количество записей.
 * @param imported успешно импортировано или обновлено.
 * @param skipped пропущено (не найдены или по стратегии пропуска конфликтов).
 * @param failed завершилось ошибкой.
 */
data class SyncResult(
    val total: Int,
    val imported: Int,
    val skipped: Int,
    val failed: Int,
)
