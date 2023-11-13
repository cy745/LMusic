package com.lalilu.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.extension.GroupAction
import com.lalilu.lmedia.extension.ListAction
import com.lalilu.lmedia.extension.ListActionPreset
import com.lalilu.lmedia.extension.OrderAction
import com.lalilu.lmedia.extension.SortAction

/**
 * 将元素的分类分组和顺序设置功能统一成一个SortPanel组件
 */
@Composable
fun SortPanel(
    sortRule: MutableState<String>,
    orderRule: MutableState<String>,
    groupRule: MutableState<String>,
    supportListAction: () -> List<ListAction>,
    onClose: () -> Unit = {}
) {
    val presets by remember { derivedStateOf { supportListAction().filterIsInstance(ListActionPreset::class.java) } }
    val sortPreset = remember {
        derivedStateOf {
            presets.firstOrNull {
                it.sortAction::class.java.name == sortRule.value &&
                        it.orderAction::class.java.name == orderRule.value &&
                        it.groupAction::class.java.name == groupRule.value
            }
        }
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
            targetState = showAdvancedOptions.value,
            label = ""
        ) { advance ->
            if (advance) {
                SongCardSortPanel(
                    sortRule = sortRule,
                    orderRule = orderRule,
                    groupRule = groupRule,
                    supportListAction = supportListAction,
                    onAdvanceCancel = { showAdvancedOptions.value = false },
                    onClose = onClose
                )
            } else {
                PresetSortPanel(
                    sortPreset = sortPreset,
                    supportSortPresets = { presets },
                    onClose = onClose,
                    onAdvance = { showAdvancedOptions.value = true },
                    onUpdateSortPreset = {
                        sortRule.value = it.sortAction::class.java.name
                        orderRule.value = it.orderAction::class.java.name
                        groupRule.value = it.groupAction::class.java.name
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PresetSortPanel(
    sortPreset: State<ListActionPreset?>,
    supportSortPresets: () -> List<ListActionPreset>,
    onUpdateSortPreset: (ListActionPreset) -> Unit,
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@Composable
fun SongCardSortPanel(
    sortRule: MutableState<String>,
    groupRule: MutableState<String>,
    orderRule: MutableState<String>,
    supportListAction: () -> List<ListAction>,
    onAdvanceCancel: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    val actions = remember { supportListAction() }
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
        actions.filterIsInstance(SortAction::class.java)
            .takeIf { it.isNotEmpty() }
            ?.let { listActions ->
                Text(text = "排序依据", style = MaterialTheme.typography.caption)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listActions.forEach { item ->
                        FilterChip(
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(5.dp),
                            onClick = { sortRule.value = item::class.java.name },
                            selected = item::class.java.name == sortRule.value,
                            colors = colors
                        ) {
                            Text(text = stringResource(id = item.titleRes))
                        }
                    }
                }
            }
        actions.filterIsInstance(GroupAction::class.java)
            .takeIf { it.isNotEmpty() }
            ?.let { listActions ->
                Text(text = "分组依据", style = MaterialTheme.typography.caption)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listActions.forEach { item ->
                        FilterChip(
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(5.dp),
                            onClick = { groupRule.value = item::class.java.name },
                            selected = item::class.java.name == groupRule.value,
                            colors = colors
                        ) {
                            Text(text = stringResource(id = item.titleRes))
                        }
                    }
                }
            }
        actions.filterIsInstance(OrderAction::class.java)
            .takeIf { it.isNotEmpty() }
            ?.let { listActions ->
                Text(text = "排序顺序", style = MaterialTheme.typography.caption)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listActions.forEach { item ->
                        FilterChip(
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(5.dp),
                            onClick = { orderRule.value = item::class.java.name },
                            selected = item::class.java.name == orderRule.value,
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