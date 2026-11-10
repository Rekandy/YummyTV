package su.afk.yummy.tv.domain.library.model

import su.afk.yummy.tv.domain.account.model.UserAnimeList

/**
 * Статус просмотра аниме во внешнем сервисе (MyAnimeList / Shikimori).
 * Служит промежуточным форматом при импорте и экспорте списков.
 */
enum class ExternalAnimeStatus {
    WATCHING,
    COMPLETED,
    ON_HOLD,
    DROPPED,
    PLAN_TO_WATCH,
}

/** Преобразует [ExternalAnimeStatus] в доменную модель [UserAnimeList]. */
fun ExternalAnimeStatus.toUserAnimeList(): UserAnimeList = when (this) {
    ExternalAnimeStatus.WATCHING -> UserAnimeList.WATCHING
    ExternalAnimeStatus.COMPLETED -> UserAnimeList.COMPLETED
    ExternalAnimeStatus.ON_HOLD -> UserAnimeList.POSTPONED
    ExternalAnimeStatus.DROPPED -> UserAnimeList.DROPPED
    ExternalAnimeStatus.PLAN_TO_WATCH -> UserAnimeList.PLANNED
}

/** Преобразует [ExternalAnimeStatus] во внутренний идентификатор списка [UserAnimeList.id]. */
fun ExternalAnimeStatus.toListId(): Int = when (this) {
    ExternalAnimeStatus.WATCHING -> UserAnimeList.WATCHING.id
    ExternalAnimeStatus.COMPLETED -> UserAnimeList.COMPLETED.id
    ExternalAnimeStatus.ON_HOLD -> UserAnimeList.POSTPONED.id
    ExternalAnimeStatus.DROPPED -> UserAnimeList.DROPPED.id
    ExternalAnimeStatus.PLAN_TO_WATCH -> UserAnimeList.PLANNED.id
}

/** Преобразует [UserAnimeList] во внешний статус [ExternalAnimeStatus]. */
fun UserAnimeList.toExternalStatus(): ExternalAnimeStatus = when (this) {
    UserAnimeList.WATCHING -> ExternalAnimeStatus.WATCHING
    UserAnimeList.COMPLETED -> ExternalAnimeStatus.COMPLETED
    UserAnimeList.POSTPONED -> ExternalAnimeStatus.ON_HOLD
    UserAnimeList.DROPPED -> ExternalAnimeStatus.DROPPED
    UserAnimeList.PLANNED -> ExternalAnimeStatus.PLAN_TO_WATCH
}
