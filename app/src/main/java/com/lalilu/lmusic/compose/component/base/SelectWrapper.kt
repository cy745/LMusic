package com.lalilu.lmusic.compose.component.base

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.utils.SelectHelper
import com.lalilu.lmusic.utils.rememberSelectState

/**
 * 将选择歌曲时展开对应的选择工具栏的逻辑封装，
 * 提供给其他处复用
 */
@Composable
fun SongsSelectWrapper(
    selector: SelectHelper<LSong> = rememberSelectState(),
    extraActionsContent: @Composable (SelectHelper<LSong>) -> Unit = {},
    content: @Composable (SelectHelper<LSong>) -> Unit
) {
    SelectWrapper(
        selector = selector,
        extraActionsContent = {
            IconTextButton(
                text = "添加到歌单",
                color = Color(0xFF3EA22C),
                onClick = {
//                    navController.navigate(PlaylistsScreenDestination(it.selectedItems.idsText()))
                }
            )
            extraActionsContent(it)
        },
        content = content
    )
}

@Composable
fun <T> SelectWrapper(
    selector: SelectHelper<T> = rememberSelectState(),
    getTipsText: (SelectHelper<T>) -> String = { "已选择: ${it.selectedItems.size}" },
    extraActionsContent: @Composable (SelectHelper<T>) -> Unit = {},
    content: @Composable (SelectHelper<T>) -> Unit
) {
    SmartBar.RegisterMainBarContent(showState = selector.isSelecting) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconTextButton(
                text = "取消",
                color = Color(0xFF006E7C),
                onClick = { selector.clear() }
            )
            Text(text = getTipsText(selector))
            extraActionsContent(selector)
        }
    }
    content(selector)
}