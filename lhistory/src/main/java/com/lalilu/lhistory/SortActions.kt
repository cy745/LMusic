package com.lalilu.lhistory

import com.lalilu.lhistory.repository.HistoryRepository
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lmedia.extension.ListAction
import com.lalilu.lmedia.extension.SortDynamicAction
import com.lalilu.lmedia.extension.Sortable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single


@Named("sort_rule_play_count")
@Single(binds = [ListAction::class])
class SortRulePlayCount(
    private val historyRepo: HistoryRepository
) : SortDynamicAction(titleRes = R.string.sort_preset_by_played_times) {

    override fun <T : Sortable> doSort(
        items: Flow<List<T>>,
        reverse: Boolean
    ): Flow<Map<GroupIdentity, List<T>>> {
        return historyRepo
            .getHistoriesIdsMapWithCount()
            .combine(items) { map, sources ->
                sources.sortedByDescending { song -> map[song.getValueBy(Sortable.COMPARE_KEY_ID)] }
                    .let { if (reverse) it.reversed() else it }
                    .let { mapOf(GroupIdentity.None to it) }
            }
    }
}

@Named("sort_rule_last_play_time")
@Single(binds = [ListAction::class])
class SortRuleLastPlayTime(
    private val historyRepo: HistoryRepository
) : SortDynamicAction(titleRes = R.string.sort_preset_by_last_play_time) {

    override fun <T : Sortable> doSort(
        items: Flow<List<T>>,
        reverse: Boolean
    ): Flow<Map<GroupIdentity, List<T>>> {
        return historyRepo
            .getHistoriesIdsMapWithLastTime()
            .combine(items) { map, sources ->
                sources.sortedByDescending { song -> map[song.getValueBy(Sortable.COMPARE_KEY_ID)] }
                    .let { if (reverse) it.reversed() else it }
                    .let { mapOf(GroupIdentity.None to it) }
            }
    }
}
