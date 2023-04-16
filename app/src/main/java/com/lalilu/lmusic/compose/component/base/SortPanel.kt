package com.lalilu.lmusic.compose.component.base

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.lalilu.R
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.OrderRule
import com.lalilu.lmedia.extension.SortRule


enum class SortPreset(
    val titleRes: Int,
    val sortRule: SortRule = SortRule.Normal,
    val orderRule: OrderRule = OrderRule.Normal,
    val groupRule: GroupRule = GroupRule.Normal
) {
    SortByAddTime(
        titleRes = R.string.sort_preset_by_add_time,
        sortRule = SortRule.CreateTime,
        groupRule = GroupRule.CreateTime
    ),
    SortByModifyTime(
        titleRes = R.string.sort_preset_by_modify_time,
        sortRule = SortRule.ModifyTime,
        groupRule = GroupRule.ModifyTime
    ),
    SortByTitle(
        titleRes = R.string.sort_preset_by_title,
        sortRule = SortRule.Title,
        groupRule = GroupRule.PinYinFirstLetter
    ),
    SortByDuration(
        titleRes = R.string.sort_preset_by_song_duration,
        sortRule = SortRule.ItemsDuration
    ),
    SortByPlayedTimes(
        titleRes = R.string.sort_preset_by_played_times,
        sortRule = SortRule.PlayCount
    ),
    SortByLastPlayTime(
        titleRes = R.string.sort_preset_by_last_play_time,
        sortRule = SortRule.LastPlayTime
    ),
    SortByItemCount(
        titleRes = R.string.sort_preset_by_item_count,
        sortRule = SortRule.ItemsCount
    ),
    SortByDiskAndTrackNumber(
        titleRes = R.string.sort_preset_by_disk_and_track,
        sortRule = SortRule.TrackNumber,
        groupRule = GroupRule.DiskNumber
    );

    companion object {
        fun from(sortRule: SortRule, groupRule: GroupRule): SortPreset? =
            from(sortRule.name, groupRule.name)

        fun from(sortRule: String, groupRule: String): SortPreset? =
            values().firstOrNull { it.sortRule.name == sortRule && it.groupRule.name == groupRule }
    }
}

/**
 * 将元素的分类分组和顺序设置功能统一成一个SortPanel组件
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SortPanel(
    sortRule: MutableState<String>,
    orderRule: MutableState<String>,
    groupRule: MutableState<String>,
    supportSortPresets: () -> List<SortPreset>,
    supportGroupRules: () -> List<GroupRule>,
    supportSortRules: () -> List<SortRule>,
    supportOrderRules: () -> List<OrderRule>,
    onClose: () -> Unit = {}
) {
    val sortPreset = remember {
        derivedStateOf { SortPreset.from(sortRule.value, groupRule.value) }
    }
    val showAdvancedOptions = remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 15.dp, end = 15.dp, bottom = 20.dp),
        border = BorderStroke(1.dp, Color.DarkGray),
        shape = RoundedCornerShape(18.dp),
        elevation = 10.dp
    ) {
        AnimatedContent(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(20.dp),
            targetState = showAdvancedOptions.value
        ) { advance ->
            if (advance) {
                SongCardSortPanel(
                    sortRule = sortRule,
                    orderRule = orderRule,
                    groupRule = groupRule,
                    supportGroupRules = supportGroupRules,
                    supportSortRules = supportSortRules,
                    supportOrderRules = supportOrderRules,
                    onAdvanceCancel = { showAdvancedOptions.value = false },
                    onClose = onClose
                )
            } else {
                PresetSortPanel(
                    sortPreset = sortPreset,
                    supportSortPresets = supportSortPresets,
                    onClose = onClose,
                    onAdvance = { showAdvancedOptions.value = true },
                    onUpdateSortPreset = {
                        sortRule.value = it.sortRule.name
                        orderRule.value = it.orderRule.name
                        groupRule.value = it.groupRule.name
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PresetSortPanel(
    sortPreset: State<SortPreset?>,
    supportSortPresets: () -> List<SortPreset>,
    onUpdateSortPreset: (SortPreset) -> Unit,
    onClose: () -> Unit = {},
    onAdvance: () -> Unit = {}
) {
    val colors = ChipDefaults.filterChipColors(
        selectedBackgroundColor = Color(0xFF029DF3),
        selectedContentColor = Color.White,
        backgroundColor = MaterialTheme.colors.onSurface
            .compositeOver(MaterialTheme.colors.surface)
            .copy(alpha = 0.05f)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "常用排序逻辑",
                style = MaterialTheme.typography.caption
            )

            supportSortPresets().forEach { preset ->
                val title = stringResource(id = preset.titleRes)
                FilterChip(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    colors = colors,
                    shape = RoundedCornerShape(5.dp),
                    selected = sortPreset.value == preset,
                    onClick = { onUpdateSortPreset(preset) },
                    trailingIcon = {
//                        Icon(
//                            modifier = Modifier.size(18.dp),
//                            contentDescription = title,
//                            painter = painterResource(
//                                id = when (preset.orderRule) {
//                                    OrderRule.Normal -> R.drawable.ic_sort_desc
//                                    OrderRule.Reverse -> R.drawable.ic_sort_asc
//                                    OrderRule.Shuffle -> R.drawable.ic_shuffle_line
//                                }
//                            ),
//                        )
                    }
                ) {
                    Text(modifier = Modifier.weight(1f), text = title)
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
        ) {
            IconTextButton(
                text = "进阶",
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(5.dp),
                color = Color(0xFFFF5722),
                onClick = onAdvance
            )

            IconTextButton(
                text = "关闭",
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(5.dp),
                color = Color(0xFF009AAD),
                onClick = onClose
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SongCardSortPanel(
    sortRule: MutableState<String>,
    groupRule: MutableState<String>,
    orderRule: MutableState<String>,
    supportGroupRules: () -> List<GroupRule>,
    supportSortRules: () -> List<SortRule>,
    supportOrderRules: () -> List<OrderRule>,
    onAdvanceCancel: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    val colors = ChipDefaults.filterChipColors(
        selectedBackgroundColor = Color(0xFF029DF3),
        selectedContentColor = Color.White,
        backgroundColor = MaterialTheme.colors.onSurface
            .compositeOver(MaterialTheme.colors.surface)
            .copy(alpha = 0.05f)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        supportSortRules()
            .takeIf { it.isNotEmpty() }
            ?.let { rules ->
                Text(text = "排序依据", style = MaterialTheme.typography.caption)
                FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                    rules.forEach { item ->
                        FilterChip(
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(5.dp),
                            onClick = { sortRule.value = item.name },
                            selected = item.name == sortRule.value,
                            colors = colors
                        ) {
                            Text(text = stringResource(id = item.titleRes))
                        }
                    }
                }
            }

        supportGroupRules()
            .takeIf { it.isNotEmpty() }
            ?.let { rules ->
                Text(text = "分组依据", style = MaterialTheme.typography.caption)
                FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                    rules.forEach { item ->
                        FilterChip(
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(5.dp),
                            onClick = { groupRule.value = item.name },
                            selected = item.name == groupRule.value,
                            colors = colors
                        ) {
                            Text(text = stringResource(id = item.titleRes))
                        }
                    }
                }
            }


        supportOrderRules()
            .takeIf { it.isNotEmpty() }
            ?.let { rules ->
                Text(text = "排序顺序", style = MaterialTheme.typography.caption)
                FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                    rules.forEach { item ->
                        FilterChip(
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(5.dp),
                            onClick = { orderRule.value = item.name },
                            selected = item.name == orderRule.value,
                            colors = colors
                        ) {
                            Text(text = stringResource(id = item.titleRes))
                        }
                    }
                }
            }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            IconTextButton(
                text = "预设",
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(5.dp),
                color = Color(0xFF4CAF50),
                onClick = onAdvanceCancel
            )

            IconTextButton(
                text = "关闭",
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(5.dp),
                color = Color(0xFF009AAD),
                onClick = onClose
            )
        }
    }
}