package su.afk.yummy.tv.domain.library.repository

import su.afk.yummy.tv.domain.library.model.ExternalListEntry
import su.afk.yummy.tv.domain.library.model.ExternalService
import su.afk.yummy.tv.domain.library.model.ParsedImportFile
import su.afk.yummy.tv.domain.library.model.ResolvedAnimeRef

/** Хранилище операций импорта и экспорта списков аниме через файлы. */
interface ListSyncRepository {
    /**
     * Разбирает файл импорта (MyAnimeList XML или Shikimori JSON) по его [uri].
     *
     * @param uri URI выбранного файла.
     * @return разобранные данные со списком аниме и определённым сервисом.
     */
    suspend fun parseImportFile(uri: String): ParsedImportFile

    /**
     * Создаёт файл экспорта в формате выбранного сервиса ([service]) для списка [entries].
     *
     * @param entries список аниме для экспорта.
     * @param service целевой сервис (MAL для XML, Shikimori для JSON).
     * @param username имя пользователя для заголовка экспорта.
     * @return путь к созданному файлу экспорта.
     */
    suspend fun buildExportFile(
        entries: List<ExternalListEntry>,
        service: ExternalService,
        username: String = "YummyTV",
    ): String

    /**
     * Ищет тайтл в каталоге YummyTV (Yani) по внешнему ID (remote_ids) или названию.
     *
     * @param entry внешняя запись (MAL / Shikimori).
     * @return данные найденного тайтла в YummyTV или null, если не найден.
     */
    suspend fun resolveYaniAnime(entry: ExternalListEntry): ResolvedAnimeRef?

    /**
     * Находит MAL ID для тайтла YummyTV по его [animeId] (для корректного экспорта).
     */
    suspend fun getMalIdForAnime(animeId: Int): Int?
}
