package com.lalilu.lplaylist.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.RemixIcon
import com.lalilu.component.LongClickableTextButton
import com.lalilu.component.base.TabScreen
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.component.base.screen.ScreenBarFactory
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.songs.SongsSearcherPanel
import com.lalilu.component.base.songs.SongsSelectorPanel
import com.lalilu.component.extension.screenVM
import com.lalilu.component.navigation.AppRouter
import com.lalilu.lplaylist.R
import com.lalilu.lplaylist.viewmodel.PlaylistsAction
import com.lalilu.lplaylist.viewmodel.PlaylistsVM
import com.lalilu.remixicon.Media
import com.lalilu.remixicon.System
import com.lalilu.remixicon.media.playListFill
import com.lalilu.remixicon.system.deleteBinLine
import com.zhangke.krouter.annotation.Destination


@Destination("/pages/playlist")
data object PlaylistScreen : TabScreen, ScreenBarFactory {
    private fun readResolve(): Any = PlaylistScreen

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = { stringResource(id = R.string.playlist_screen_title) },
            icon = RemixIcon.Media.playListFill,
        )
    }

    @Composable
    override fun Content() {
        val vm = screenVM<PlaylistsVM>()
        val state by vm.state

        SongsSearcherPanel(
            isVisible = { state.showSearcherPanel },
            onDismiss = { vm.intent(PlaylistsAction.HideSearcherPanel) },
            keyword = { state.searchKeyWord },
            onUpdateKeyword = { vm.intent(PlaylistsAction.SearchFor(it)) }
        )

        SongsSelectorPanel(
            isVisible = { vm.selector.isSelecting.value },
            onDismiss = { vm.selector.isSelecting.value = false },
            screenActions = listOfNotNull(
                ScreenAction.Dynamic {
                    val color = Color(0xFFFF3C3C)

                    LongClickableTextButton(
                        modifier = Modifier.fillMaxHeight(),
                        shape = RectangleShape,
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = color.copy(alpha = 0.15f),
                            contentColor = color
                        ),
                        onLongClick = { vm.intent(PlaylistsAction.TryRemovePlaylist(vm.selector.selected())) },
                        onClick = { ToastUtils.showShort("请长按此按钮以继续") },
                    ) {
                        Image(
                            modifier = Modifier.size(20.dp),
                            imageVector = RemixIcon.System.deleteBinLine,
                            contentDescription = "删除歌单",
                            colorFilter = ColorFilter.tint(color = color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "删除歌单",
                            fontSize = 14.sp
                        )
                    }
                }
            )
        )

        PlaylistScreenContent(
            isSearching = { state.searchKeyWord.isNotBlank() && !state.showSearcherPanel },
            onStartSearch = { vm.intent(PlaylistsAction.ShowSearcherPanel) },
            isSelected = { vm.selector.isSelected(it) },
            isSelecting = { vm.selector.isSelecting.value },
            playlists = { vm.playlists.value },
            onUpdatePlaylist = { vm.intent(PlaylistsAction.UpdatePlaylist(it)) },
            onLongClickPlaylist = { vm.selector.onSelect(it) },
            onClickPlaylist = {
                if (vm.selector.isSelecting.value) {
                    vm.selector.onSelect(it)
                } else {
                    AppRouter.route("/pages/playlist/detail")
                        .with("playlistId", it.id)
                        .push()
                }
            }
        )
    }
}