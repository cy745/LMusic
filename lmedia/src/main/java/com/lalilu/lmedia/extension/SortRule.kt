package com.lalilu.lmedia.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.lalilu.lmedia.R
import com.lalilu.lmedia.extension.sortable.ActionInfo
import com.lalilu.lmedia.extension.sortable.GroupId
import com.lalilu.lmedia.extension.sortable.ItemExtraData
import com.lalilu.lmedia.extension.sortable.SortAction
import com.lalilu.lmedia.extension.sortable.SortConfig
import com.lalilu.lmedia.extension.sortable.SortResult
import com.lalilu.lmedia.extension.sortable.Sortable
import com.lalilu.lmedia.extension.sortable.SortedGroup
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.text.Collator


@Named("sort_rule_normal")
@Single(binds = [SortAction::class])
class SortRuleNormal : SortAction {
    override fun key(): String = "sort_rule_normal"

    @Composable
    override fun getActionInfo(): ActionInfo = ActionInfo(
        title = stringResource(R.string.sort_preset_by_normal),
        subTitle = "根据歌曲添加时间"
    )
}

@Named("sort_rule_add_time")
@Single(binds = [SortAction::class])
class AddTime : SortAction {
    override fun key(): String = "sort_rule_add_time"

    private val timeStrJustNow: String? by lazy { StringUtils.getString(R.string.group_identity_time_just_now) }
    private val timeStrMinutesAgo: String? by lazy { StringUtils.getString(R.string.group_identity_time_minutes_ago) }
    private val timeStrHoursAgo: String? by lazy { StringUtils.getString(R.string.group_identity_time_hours_ago) }
    private val timeStrExactDay: String? by lazy { StringUtils.getString(R.string.group_identity_time_exact_day_pattern) }

    @Composable
    override fun getActionInfo(): ActionInfo = ActionInfo(
        title = stringResource(R.string.sort_preset_by_add_time)
    )

    override fun <T : Sortable> doSortInternal(
        items: List<T>,
        config: SortConfig
    ): SortResult<T> {
        val now = System.currentTimeMillis()

        val sorted = items
            .sortedByDescending { (it.getValueBy(Sortable.COMPARE_KEY_CREATE_TIME) ?: -1L) }
            .let { if (config.reverse) it.asReversed() else it }

        val grouped = sorted
            .groupBy { item ->
                val time = (item.getValueBy(Sortable.COMPARE_KEY_CREATE_TIME) ?: -1L) * 1000
                when {
                    now - time < 300000 -> timeStrJustNow
                    now - time < 3600000 -> timeStrMinutesAgo?.format((now - time) / 60000)
                    now - time < 86400000 -> timeStrHoursAgo?.format((now - time) / 3600000)
                    else -> timeStrExactDay?.let { TimeUtils.millis2String(time, it) }
                }
            }

        return grouped.let {
            SortResult.Grouped(it.map { map ->
                SortedGroup(
                    groupId = GroupId.Time(map.key ?: "#"),
                    items = map.value
                )
            })
        }
    }
}

@Named("sort_rule_title")
@Single(binds = [SortAction::class])
class Title : SortAction {
    override fun key(): String = "sort_rule_title"

    @Composable
    override fun getActionInfo(): ActionInfo = ActionInfo(
        title = stringResource(R.string.sort_preset_by_title)
    )

    override fun <T : Sortable> doSortInternal(
        items: List<T>,
        config: SortConfig
    ): SortResult<T> {
        val sorted = items.sortedWith { a, b ->
            val aText = a.getValueBy<String>(Sortable.COMPARE_KEY_TITLE) ?: return@sortedWith 0
            val bText = b.getValueBy<String>(Sortable.COMPARE_KEY_TITLE) ?: return@sortedWith 0

            Collator.getInstance().compare(aText, bText)
        }.let { if (config.reverse) it.asReversed() else it }

        val grouped = sorted.groupBy {
            val text = it.getValueBy<String>(Sortable.COMPARE_KEY_TITLE)
            PinyinUtils.getPinyinFirstLetter(text)?.uppercase() ?: ""
        }

        return grouped.let {
            SortResult.Grouped(it.map { map ->
                SortedGroup(
                    groupId = GroupId.FirstLetter(map.key),
                    items = map.value,
                )
            })
        }
    }
}

@Named("sort_rule_duration")
@Single(binds = [SortAction::class])
class Duration : SortAction {
    override fun key(): String = "sort_rule_duration"

    @Composable
    override fun getActionInfo(): ActionInfo = ActionInfo(
        title = stringResource(R.string.sort_preset_by_song_duration)
    )

    override fun <T : Sortable> doSortInternal(
        items: List<T>,
        config: SortConfig
    ): SortResult<T> {
        val sorted = items
            .sortedByDescending { it.getValueBy(Sortable.COMPARE_KEY_DURATION) ?: -1L }
            .let { if (config.reverse) it.asReversed() else it }

        return SortResult.Flat(sorted)
    }
}


@Named("sort_rule_shuffle")
@Single(binds = [SortAction::class])
class Shuffle : SortAction {
    override fun key(): String = "sort_rule_shuffle"

    @Composable
    override fun getActionInfo(): ActionInfo = ActionInfo(
        title = stringResource(R.string.sort_preset_by_shuffle)
    )

    override fun <T : Sortable> doSortInternal(
        items: List<T>,
        config: SortConfig
    ): SortResult<T> {
        val shuffled = items.shuffled()
            .let { if (config.reverse) it.asReversed() else it }

        return SortResult.Flat(shuffled)
    }
}

/**
 * 元素内歌曲数量排序
 */
@Named("sort_rule_items_count")
@Single(binds = [SortAction::class])
class ItemsCount : SortAction {
    override fun key(): String = "sort_rule_items_count"

    @Composable
    override fun getActionInfo(): ActionInfo = ActionInfo(
        title = stringResource(R.string.sort_preset_by_item_count)
    )

    override fun <T : Sortable> doSortInternal(
        items: List<T>,
        config: SortConfig
    ): SortResult<T> {
        val sorted = items
            .sortedByDescending { it.getValueBy(Sortable.COMPARE_KEY_ITEMS_COUNT) ?: 0L }
            .let { if (config.reverse) it.asReversed() else it }

        return SortResult.Flat(sorted)
    }
}


@Named("sort_rule_album")
@Single(binds = [SortAction::class])
class Album : SortAction {
    override fun key(): String = "sort_rule_album"

    @Composable
    override fun getActionInfo(): ActionInfo = ActionInfo(
        title = stringResource(R.string.sort_preset_by_disk_and_track)
    )

    override fun <T : Sortable> doSortInternal(
        items: List<T>,
        config: SortConfig
    ): SortResult<T> {
        val grouped = items.groupBy {
            it.getValueBy<String>(Sortable.COMPARE_KEY_DISK_NUMBER)
                ?.toIntOrNull()
                ?: -1
        }

        return SortResult.Grouped(grouped.map { map ->
            val list = map.value.sortedBy {
                it.getValueBy<String>(Sortable.COMPARE_KEY_TRACK_NUMBER)
                    ?.toIntOrNull()
                    ?: 0
            }.let { if (config.reverse) it.asReversed() else it }

            val extras: List<ItemExtraData> = list
                .takeIf { !config.hideItemExtra }
                ?.map {
                    val trackNum = it.getValueBy<String>(Sortable.COMPARE_KEY_TRACK_NUMBER)
                        ?.toIntOrNull()
                        ?: 0
                    ItemExtraData.TrackNumber(trackNum)
                } ?: emptyList()

            SortedGroup(
                groupId = map.key.takeIf { it >= 0 }?.let { GroupId.DiskNumber(it) },
                extras = extras,
                items = list
            )
        })
    }
}