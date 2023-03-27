package com.lalilu.lmusic.compose.component.base

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.OrderRule
import com.lalilu.lmedia.extension.SortRule
import com.lalilu.lmusic.datastore.LMusicSp
import org.koin.androidx.compose.get

/**
 * 将元素的分类分组和顺序设置功能统一成一个SortPanel组件
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SortPanel(
    lMusicSp: LMusicSp = get(),
    sortFor: String,
    supportGroupRules: () -> List<GroupRule>,
    supportSortRules: () -> List<SortRule>,
    supportOrderRules: () -> List<OrderRule>,
    onClose: () -> Unit = {}
) {
    var sortRule by lMusicSp.stringSp("${sortFor}_SORT_RULE", SortRule.Normal.name)
    var orderRule by lMusicSp.stringSp("${sortFor}_ORDER_RULE", OrderRule.Normal.name)
    var groupRule by lMusicSp.stringSp("${sortFor}_GROUP_RULE", GroupRule.Normal.name)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 24.dp, bottom = 20.dp, start = 12.dp, end = 12.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        supportSortRules()
            .takeIf { it.isNotEmpty() }
            ?.let { rules ->
                Text(text = "排序依据")
                FlowRow(mainAxisSpacing = 8.dp) {
                    rules.forEach { item ->
                        FilterChip(
                            onClick = { sortRule = item.name },
                            selected = item.name == sortRule,
                            colors = ChipDefaults.filterChipColors(),
                        ) {
                            Text(text = item.title)
                        }
                    }
                }
            }

        supportGroupRules()
            .takeIf { it.isNotEmpty() }
            ?.let { rules ->
                Text(text = "分组依据")
                FlowRow(mainAxisSpacing = 8.dp) {
                    rules.forEach { item ->
                        FilterChip(
                            onClick = { groupRule = item.name },
                            selected = item.name == groupRule,
                            colors = ChipDefaults.filterChipColors(),
                        ) {
                            Text(text = item.title)
                        }
                    }
                }
            }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                supportOrderRules()
                    .takeIf { it.isNotEmpty() }
                    ?.let { rules ->
                        Text(text = "排序顺序")
                        FlowRow(mainAxisSpacing = 8.dp) {
                            rules.forEach { item ->
                                FilterChip(
                                    onClick = { orderRule = item.name },
                                    selected = item.name == orderRule,
                                    colors = ChipDefaults.filterChipColors(),
                                ) {
                                    Text(text = item.title)
                                }
                            }
                        }
                    }
            }
            IconTextButton(
                text = "关闭",
                color = Color(0xFF006E7C),
                onClick = onClose
            )
        }
    }
}