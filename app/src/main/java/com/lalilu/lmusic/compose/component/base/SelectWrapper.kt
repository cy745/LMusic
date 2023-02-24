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
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.screen.LibraryDetailNavigateBar
import com.lalilu.lmusic.utils.SelectHelper
import com.lalilu.lmusic.utils.extension.LaunchedDisposeEffect
import com.lalilu.lmusic.utils.rememberSelectState
import com.lalilu.lmusic.viewmodel.MainViewModel
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel
import okhttp3.internal.toImmutableList
import org.koin.androidx.compose.get

/**
 * 将选择歌曲时展开对应的选择工具栏的逻辑封装，
 * 提供给其他处复用
 */
@Composable
fun SongsSelectWrapper(
    selector: SelectHelper<LSong> = rememberSelectState(),
    mainVM: MainViewModel = get(),
    recoverTo: @Composable () -> Unit = LibraryDetailNavigateBar,
    extraActionsContent: @Composable (SelectHelper<LSong>) -> Unit = {},
    content: @Composable (SelectHelper<LSong>) -> Unit
) {
    val navToAddToPlaylist = mainVM.navToAddToPlaylist()

    SelectWrapper(
        selector = selector,
        recoverTo = recoverTo,
        extraActionsContent = {
            IconTextButton(
                text = "添加到歌单",
                color = Color(0xFF3EA22C),
                onClick = { navToAddToPlaylist(it.selectedItems) }
            )
            extraActionsContent(it)
        },
        content = content
    )
}

@Composable
fun PlaylistsSelectWrapper(
    isAddingSongs: Boolean = false,
    selector: SelectHelper<LPlaylist> = rememberSelectState(),
    mainVM: MainViewModel = get(),
    playlistsVM: PlaylistsViewModel = get(),
    recoverTo: @Composable () -> Unit = LibraryDetailNavigateBar,
    extraActionsContent: @Composable (SelectHelper<LPlaylist>) -> Unit = {},
    content: @Composable (SelectHelper<LPlaylist>) -> Unit
) {
    SelectWrapper(
        selector = selector,
        recoverTo = recoverTo,
        getTipsText = {
            if (isAddingSongs) {
                "${mainVM.getTempSong().size}首歌 -> ${it.selectedItems.size}歌单"
            } else {
                "已选择: ${it.selectedItems.size}"
            }
        },
        extraActionsContent = {
            if (isAddingSongs) {
                IconTextButton(
                    text = "确认保存",
                    color = Color(0xFF3EA22C),
                    onClick = {
                        playlistsVM.addSongsIntoPlaylists(
                            playlists = it.selectedItems.toImmutableList(),
                            songs = mainVM.getTempSong().toImmutableList()
                        )
                        it.clear()
                    }
                )
            } else {
                IconTextButton(
                    text = "删除",
                    color = Color(0xFF006E7C),
                    onClick = {
                        playlistsVM.removePlaylists(it.selectedItems.toImmutableList())
                        it.clear()
                    }
                )
            }
            extraActionsContent(it)
        },
        content = content
    )
}

@Composable
fun <T> SelectWrapper(
    selector: SelectHelper<T> = rememberSelectState(),
    getTipsText: (SelectHelper<T>) -> String = { "已选择: ${it.selectedItems.size}" },
    recoverTo: @Composable () -> Unit = LibraryDetailNavigateBar,
    extraActionsContent: @Composable (SelectHelper<T>) -> Unit = {},
    content: @Composable (SelectHelper<T>) -> Unit
) {
    LaunchedDisposeEffect(
        key = { selector.isSelecting.value }
    ) {
        if (it) {
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
                    Text(text = getTipsText(selector))
                    extraActionsContent(selector)
                }
            }
        } else {
            SmartBar.setMainBar(content = recoverTo)
        }
    }
    content(selector)
}