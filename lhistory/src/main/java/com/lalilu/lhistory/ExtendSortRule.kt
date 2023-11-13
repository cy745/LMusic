package com.lalilu.lhistory

import com.lalilu.component.R
import com.lalilu.lhistory.repository.HistoryRepository
import com.lalilu.lmedia.extension.ListActionPreset
import com.lalilu.lmedia.extension.SortRuleDynamic
import com.lalilu.lmedia.extension.Sortable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.java.KoinJavaComponent

object SortRulePlayCount :
    SortRuleDynamic(titleRes = com.lalilu.lmedia.R.string.sort_rule_play_count) {

    private val historyRepo: HistoryRepository by KoinJavaComponent.inject(HistoryRepository::class.java)

    override fun <T : Sortable> sort(items: Flow<List<T>>): Flow<List<T>> {
        return historyRepo
            .getHistoriesIdsMapWithCount()
            .combine(items) { map, sources ->
                sources.sortedByDescending { song -> map[song.requireId()] }
            }
    }
}

object SortRuleLastPlayTime :
    SortRuleDynamic(titleRes = com.lalilu.lmedia.R.string.sort_rule_last_play_time) {

    private val historyRepo: HistoryRepository by KoinJavaComponent.inject(HistoryRepository::class.java)

    override fun <T : Sortable> sort(items: Flow<List<T>>): Flow<List<T>> {
        return historyRepo
            .getHistoriesIdsMapWithLastTime()
            .combine(items) { map, sources ->
                sources.sortedByDescending { song -> map[song.requireId()] }
            }
    }
}

object SortRulePresetPlayedTimes : ListActionPreset(
    titleRes = R.string.sort_preset_by_played_times,
    sortAction = SortRulePlayCount
)

object SortRulePresetLastPlayTime : ListActionPreset(
    titleRes = R.string.sort_preset_by_last_play_time,
    sortAction = SortRuleLastPlayTime
)