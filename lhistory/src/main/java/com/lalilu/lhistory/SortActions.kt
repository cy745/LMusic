package com.lalilu.lhistory

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.lalilu.lhistory.repository.HistoryRepository
import com.lalilu.lmedia.extension.sortable.ActionInfo
import com.lalilu.lmedia.extension.sortable.ItemExtraData
import com.lalilu.lmedia.extension.sortable.SortAction
import com.lalilu.lmedia.extension.sortable.SortConfig
import com.lalilu.lmedia.extension.sortable.SortResult
import com.lalilu.lmedia.extension.sortable.Sortable
import com.lalilu.lmedia.extension.sortable.SortedGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single


@Named("sort_rule_play_count")
@Single(binds = [SortAction::class])
class SortRulePlayCount(
    private val historyRepo: HistoryRepository
) : SortAction {
    override fun key(): String = "sort_rule_play_count"

    @Composable
    override fun getActionInfo(): ActionInfo = ActionInfo(
        title = stringResource(R.string.sort_preset_by_played_times),
        subTitle = "历史记录中播放次数排序"
    )

    override fun <T : Sortable> doSort(
        items: Flow<List<T>>,
        config: SortConfig,
    ): Flow<SortResult<T>> {
        return historyRepo
            .getHistoriesIdsMapWithCount()
            .combine(items) { map, sources ->
                val sorted = sources
                    .sortedByDescending { song -> map[song.getValueBy(Sortable.COMPARE_KEY_ID)] }
                    .let { if (config.reverse) it.reversed() else it }

                if (config.hideItemExtra) {
                    SortResult.flat(sorted)
                } else {
                    val extras = sorted.map {
                        ItemExtraData.PlayedCount(
                            count = map[it.getValueBy(Sortable.COMPARE_KEY_ID)] ?: 0
                        )
                    }

                    SortResult(
                        groups = listOf(
                            SortedGroup(
                                groupId = null,
                                extras = extras,
                                items = sorted
                            )
                        )
                    )
                }
            }
    }
}

@Named("sort_rule_last_play_time")
@Single(binds = [SortAction::class])
class SortRuleLastPlayTime(
    private val historyRepo: HistoryRepository
) : SortAction {
    override fun key(): String = "sort_rule_last_play_time"

    @Composable
    override fun getActionInfo(): ActionInfo = ActionInfo(
        title = stringResource(R.string.sort_preset_by_last_play_time),
        subTitle = "历史记录播放排序"
    )

    override fun <T : Sortable> doSort(
        items: Flow<List<T>>,
        config: SortConfig,
    ): Flow<SortResult<T>> {
        return historyRepo
            .getHistoriesIdsMapWithLastTime()
            .combine(items) { map, sources ->
                val sorted = sources
                    .sortedByDescending { song -> map[song.getValueBy(Sortable.COMPARE_KEY_ID)] }
                    .let { if (config.reverse) it.reversed() else it }

                SortResult.flat(sorted)
            }
    }
}
