package su.afk.yummy.tv.domain.library.usecase

import su.afk.yummy.tv.domain.library.model.ExternalListEntry
import su.afk.yummy.tv.domain.library.model.ExternalService
import su.afk.yummy.tv.domain.library.repository.ListSyncRepository
import javax.inject.Inject

/** Формирует файл экспорта локальной библиотеки в формате MyAnimeList (XML) или Shikimori (JSON). */
class BuildExportFileUseCase @Inject constructor(
    private val repository: ListSyncRepository,
) {
    suspend operator fun invoke(
        entries: List<ExternalListEntry>,
        service: ExternalService,
        username: String = "YummyTV",
    ): String = repository.buildExportFile(entries, service, username)
}
