package su.afk.yummy.tv.domain.library.usecase

import su.afk.yummy.tv.domain.account.model.UserAnimeList
import su.afk.yummy.tv.domain.library.model.ExternalListEntry
import su.afk.yummy.tv.domain.library.model.toExternalStatus
import su.afk.yummy.tv.domain.library.repository.LibraryRepository
import su.afk.yummy.tv.domain.library.repository.ListSyncRepository
import javax.inject.Inject

/** Преобразует локальную библиотеку пользователя в список записей внешнего формата для последующего экспорта. */
class GetLocalLibraryAsExternalEntriesUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val listSyncRepository: ListSyncRepository,
) {
    suspend operator fun invoke(): List<ExternalListEntry> {
        return libraryRepository.getAll()
            .filter { it.listId >= 0 }
            .mapNotNull { item ->
                val list = UserAnimeList.entries.find { it.id == item.listId } ?: return@mapNotNull null
                val malId = listSyncRepository.getMalIdForAnime(item.animeId)
                ExternalListEntry(
                    externalId = malId ?: item.animeId,
                    title = item.title,
                    status = list.toExternalStatus(),
                    episodesWatched = 0,
                    score = item.userRating,
                )
            }
    }
}
