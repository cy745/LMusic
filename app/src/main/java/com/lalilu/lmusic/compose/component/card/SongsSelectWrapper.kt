package com.lalilu.lmusic.compose.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.base.IconTextButton
import com.lalilu.lmusic.compose.screen.LibraryDetailNavigateBar
import com.lalilu.lmusic.utils.SelectHelper
import com.lalilu.lmusic.utils.rememberSelectState
import com.lalilu.lmusic.viewmodel.LocalMainVM
import com.lalilu.lmusic.viewmodel.MainViewModel

/**
 * 将选择歌曲时展开对应的选择工具栏的逻辑封装，
 * 提供给其他处复用
 */
@Composable
fun SongsSelectWrapper(
    mainVM: MainViewModel = LocalMainVM.current,
    recoverTo: @Composable () -> Unit = LibraryDetailNavigateBar,
    extraActionsContent: @Composable (SelectHelper<LSong>) -> Unit = {},
    content: @Composable (SelectHelper<LSong>) -> Unit
) {
    val selector = rememberSelectState<LSong>()
    val navToAddToPlaylist = mainVM.navToAddToPlaylist()

    LaunchedEffect(selector.isSelecting.value) {
        if (selector.isSelecting.value) {
            SmartBar.setMainBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    IconTextButton(
                        text = "取消",
                        color = Color(0xFF006E7C),
                        onClick = { selector.clear() }
                    )
                    Text(text = "已选择: ${selector.selectedItems.size}")
                    IconTextButton(
                        text = "添加到歌单",
                        color = Color(0xFF3EA22C),
                        onClick = { navToAddToPlaylist(selector.selectedItems) }
                    )
                    extraActionsContent(selector)
                }
            }
        } else {
            SmartBar.setMainBar(item = recoverTo)
        }
    }
    content(selector)
}