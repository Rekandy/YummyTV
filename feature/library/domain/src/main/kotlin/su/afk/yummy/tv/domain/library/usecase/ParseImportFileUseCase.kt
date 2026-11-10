package su.afk.yummy.tv.domain.library.usecase

import su.afk.yummy.tv.domain.library.model.ParsedImportFile
import su.afk.yummy.tv.domain.library.repository.ListSyncRepository
import javax.inject.Inject

/** Разбирает файл импорта (MyAnimeList XML или Shikimori JSON) и возвращает распознанные записи. */
class ParseImportFileUseCase @Inject constructor(
    private val repository: ListSyncRepository,
) {
    suspend operator fun invoke(uri: String): ParsedImportFile =
        repository.parseImportFile(uri)
}
