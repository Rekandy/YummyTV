package su.afk.yummy.tv.domain.library.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import su.afk.yummy.tv.core.common.coroutines.runSuspendCatching
import su.afk.yummy.tv.core.model.mutation.PendingMutation
import su.afk.yummy.tv.core.model.mutation.PendingMutationQueue
import su.afk.yummy.tv.domain.account.model.UserAnimeList
import su.afk.yummy.tv.domain.account.usecase.GetAccountSessionUseCase
import su.afk.yummy.tv.domain.account.usecase.SetAnimeListUseCase
import su.afk.yummy.tv.domain.account.usecase.SetAnimeRatingUseCase
import su.afk.yummy.tv.domain.library.model.ImportConflictStrategy
import su.afk.yummy.tv.domain.library.model.LibraryItem
import su.afk.yummy.tv.domain.library.model.SyncPreviewItem
import su.afk.yummy.tv.domain.library.model.SyncResult
import su.afk.yummy.tv.domain.library.model.toListId
import su.afk.yummy.tv.domain.library.model.toUserAnimeList
import su.afk.yummy.tv.domain.library.repository.LibraryRepository
import javax.inject.Inject

/** Импортирует выбранные записи в библиотеку и синхронизирует с удалённым аккаунтом YummyTV. */
class ImportEntriesUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val getAccountSession: GetAccountSessionUseCase,
    private val setAnimeList: SetAnimeListUseCase,
    private val setAnimeRating: SetAnimeRatingUseCase,
    private val pendingMutationQueue: PendingMutationQueue,
    private val syncRemoteLibrary: SyncRemoteLibraryUseCase,
) {
    suspend operator fun invoke(
        items: List<SyncPreviewItem>,
        strategy: ImportConflictStrategy,
        skipUnmatched: Boolean = false,
    ): SyncResult {
        var imported = 0
        var skipped = 0
        var failed = 0

        val localItems = libraryRepository.getAll().associateBy { it.animeId }
        val toSyncWithAccount = mutableListOf<AccountSyncTarget>()

        for (item in items) {
            val matchedId = item.localAnimeId

            // Тайтл не найден в каталоге YummyTV — пропускаем, чтобы не засорять базу невалидными ID (приводящими к 404)
            if (matchedId == null) {
                skipped++
                continue
            }

            val existing = localItems[matchedId]

            // Если тайтл уже есть в библиотеке и есть конфликт
            if (existing != null && item.hasConflict) {
                if (strategy == ImportConflictStrategy.SKIP) {
                    skipped++
                    continue
                }
            }

            // Добавляем новый или обновляем существующий тайтл
            runCatching {
                val now = System.currentTimeMillis()
                val targetUserList = item.entry.status.toUserAnimeList()
                val targetListId = targetUserList.id
                val score = item.entry.score

                val isNew = existing == null
                val isListChanged = existing != null && existing.listId != targetListId
                val isRatingChanged = score != null && score in 1..10 && existing?.userRating != score

                val newItem = LibraryItem(
                    animeId = matchedId,
                    title = item.localTitle ?: existing?.title ?: item.entry.title,
                    poster = existing?.poster ?: item.resolvedAnime?.poster,
                    addedAt = existing?.addedAt ?: now,
                    listId = targetListId,
                    isFavorite = existing?.isFavorite ?: false,
                    listUpdatedAt = if (existing == null || isListChanged) now else existing.listUpdatedAt,
                    favoriteUpdatedAt = existing?.favoriteUpdatedAt ?: 0L,
                    userRating = score ?: existing?.userRating,
                    year = existing?.year ?: item.resolvedAnime?.year,
                    rating = existing?.rating ?: item.resolvedAnime?.rating,
                    nextEpisodeAtSeconds = existing?.nextEpisodeAtSeconds,
                    season = existing?.season,
                    kind = existing?.kind,
                    releaseStatus = existing?.releaseStatus,
                )
                libraryRepository.add(newItem)

                if (isNew || isListChanged || isRatingChanged) {
                    toSyncWithAccount.add(
                        AccountSyncTarget(
                            animeId = matchedId,
                            userList = targetUserList.takeIf { isNew || isListChanged },
                            score = score.takeIf { (isNew && score in 1..10) || isRatingChanged },
                        )
                    )
                }
            }.onSuccess {
                imported++
            }.onFailure {
                failed++
            }
        }

        // Синхронизируем с удалённым аккаунтом YummyTV (если пользователь вошёл)
        syncWithRemoteAccount(toSyncWithAccount)

        return SyncResult(
            total = items.size,
            imported = imported,
            skipped = skipped,
            failed = failed,
        )
    }

    private suspend fun syncWithRemoteAccount(targets: List<AccountSyncTarget>) {
        if (targets.isEmpty()) return

        val session = runSuspendCatching { getAccountSession() }.getOrNull()
        if (session == null || !session.isAuthorized) return

        val semaphore = Semaphore(4)
        coroutineScope {
            targets.map { target ->
                async {
                    semaphore.withPermit {
                        target.userList?.let { userList ->
                            val listResult = runSuspendCatching { setAnimeList(target.animeId, userList) }
                            if (listResult.isFailure) {
                                val error = listResult.exceptionOrNull() ?: Exception()
                                runSuspendCatching {
                                    pendingMutationQueue.enqueueOnNetworkFailure(
                                        PendingMutation.SetList(target.animeId, userList.id),
                                        error
                                    )
                                }
                            }
                        }

                        target.score?.let { score ->
                            val ratingResult = runSuspendCatching { setAnimeRating(target.animeId, score) }
                            if (ratingResult.isFailure) {
                                val error = ratingResult.exceptionOrNull() ?: Exception()
                                runSuspendCatching {
                                    pendingMutationQueue.enqueueOnNetworkFailure(
                                        PendingMutation.SetRating(target.animeId, score),
                                        error
                                    )
                                }
                            }
                        }
                    }
                }
            }.awaitAll()
        }

        // Запускаем мягкую синхронизацию (forceRefresh = false), чтобы обновить снимок без удаления
        runSuspendCatching { syncRemoteLibrary(session.userId, forceRefresh = false) }
    }

    private data class AccountSyncTarget(
        val animeId: Int,
        val userList: UserAnimeList?,
        val score: Int?,
    )
}
