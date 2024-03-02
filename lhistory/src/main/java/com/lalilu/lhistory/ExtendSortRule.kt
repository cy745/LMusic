package com.lalilu.lhistory

import com.lalilu.lhistory.repository.HistoryRepository
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lmedia.extension.SortDynamicAction
import com.lalilu.lmedia.extension.Sortable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.java.KoinJavaComponent

object SortRulePlayCount :
    SortDynamicAction(titleRes = R.string.sort_preset_by_played_times) {

    private val historyRepo: HistoryRepository by KoinJavaComponent.inject(HistoryRepository::class.java)

    override fun <T : Sortable> doSort(
        items: Flow<List<T>>,
        reverse: Boolean
    ): Flow<Map<GroupIdentity, List<T>>> {
        return historyRepo
            .getHistoriesIdsMapWithCount()
            .combine(items) { map, sources ->
                sources.sortedByDescending { song -> map[song.requireId()] }
                    .let { if (reverse) it.reversed() else it }
                    .let { mapOf(GroupIdentity.None to it) }
            }
    }
}

object SortRuleLastPlayTime :
    SortDynamicAction(titleRes = R.string.sort_preset_by_last_play_time) {

    private val historyRepo: HistoryRepository by KoinJavaComponent.inject(HistoryRepository::class.java)

    override fun <T : Sortable> doSort(
        items: Flow<List<T>>,
        reverse: Boolean
    ): Flow<Map<GroupIdentity, List<T>>> {
        return historyRepo
            .getHistoriesIdsMapWithLastTime()
            .combine(items) { map, sources ->
                sources.sortedByDescending { song -> map[song.requireId()] }
                    .let { if (reverse) it.reversed() else it }
                    .let { mapOf(GroupIdentity.None to it) }
            }
    }
}
