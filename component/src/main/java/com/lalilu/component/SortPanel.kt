package com.lalilu.component

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.extension.ListAction

/**
 * 将元素的分类分组和顺序设置功能统一成一个SortPanel组件
 */
@Composable
fun SortPanel(
    sortRule: MutableState<String>,
    reverseOrder: MutableState<Boolean>,
    flattenOverride: MutableState<Boolean>,
    supportListAction: () -> List<ListAction>,
    onClose: () -> Unit = {}
) {
    val supportPresets by remember { derivedStateOf { supportListAction() } }
    val currentPreset = remember {
        derivedStateOf {
            supportPresets.firstOrNull { it::class.java.name == sortRule.value }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 15.dp, end = 15.dp, bottom = 20.dp),
        border = BorderStroke(1.dp, Color.DarkGray),
        shape = RoundedCornerShape(18.dp),
        elevation = 10.dp
    ) {
        PresetSortPanel(
            modifier = Modifier.padding(20.dp),
            sortPreset = currentPreset,
            reverseOrder = reverseOrder,
            flattenOverride = flattenOverride,
            supportSortPresets = { supportPresets },
            onReverseOrderUpdate = { reverseOrder.value = it },
            onFlattenOverrideUpdate = { flattenOverride.value = it },
            onUpdateSortPreset = { sortRule.value = it::class.java.name },
            onClose = onClose
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PresetSortPanel(
    modifier: Modifier = Modifier,
    sortPreset: State<ListAction?>,
    reverseOrder: State<Boolean>,
    flattenOverride: State<Boolean>,
    supportSortPresets: () -> List<ListAction>,
    onReverseOrderUpdate: (Boolean) -> Unit = {},
    onFlattenOverrideUpdate: (Boolean) -> Unit = {},
    onUpdateSortPreset: (ListAction) -> Unit,
    onClose: () -> Unit = {}
) {
    val colors = ChipDefaults.filterChipColors(
        selectedBackgroundColor = Color(0xFF029DF3),
        selectedContentColor = Color.White,
        backgroundColor = MaterialTheme.colors.onSurface
            .compositeOver(MaterialTheme.colors.surface)
            .copy(alpha = 0.05f)
    )

    Row(
        modifier = modifier
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

        val animateColorForFlattenOverride = animateColorAsState(
            targetValue = if (flattenOverride.value) Color.LightGray else Color(0xFF9CAD00),
            label = ""
        )

        val animateColorForReverseOrder = animateColorAsState(
            targetValue = if (reverseOrder.value) Color(0xFFFFAA00) else Color.LightGray,
            label = ""
        )

        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
        ) {
            IconTextButton(
                text = "分组",
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(5.dp),
                color = animateColorForFlattenOverride.value,
                onClick = { onFlattenOverrideUpdate(!flattenOverride.value) }
            )
            IconTextButton(
                text = "倒序",
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(5.dp),
                color = animateColorForReverseOrder.value,
                onClick = { onReverseOrderUpdate(!reverseOrder.value) }
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