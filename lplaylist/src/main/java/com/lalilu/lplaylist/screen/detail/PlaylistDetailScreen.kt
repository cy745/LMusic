package com.lalilu.lplaylist.screen.detail

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
import cafe.adriel.voyager.core.screen.Screen
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.RemixIcon
import com.lalilu.common.ext.requestFor
import com.lalilu.component.LongClickableTextButton
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.component.base.screen.ScreenActionFactory
import com.lalilu.component.base.screen.ScreenBarFactory
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.base.songs.SongsHeaderJumperDialog
import com.lalilu.component.base.songs.SongsSearcherPanel
import com.lalilu.component.base.songs.SongsSelectorPanel
import com.lalilu.component.base.songs.SongsSortPanelDialog
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.component.extension.screenVM
import com.lalilu.lmedia.extension.SortStaticAction
import com.lalilu.lplaylist.R
import com.lalilu.lplaylist.viewmodel.PlaylistDetailAction
import com.lalilu.lplaylist.viewmodel.PlaylistDetailVM
import com.lalilu.remixicon.Design
import com.lalilu.remixicon.Editor
import com.lalilu.remixicon.System
import com.lalilu.remixicon.design.editBoxLine
import com.lalilu.remixicon.design.focus3Line
import com.lalilu.remixicon.editor.sortDesc
import com.lalilu.remixicon.system.checkboxMultipleBlankLine
import com.lalilu.remixicon.system.checkboxMultipleLine
import com.lalilu.remixicon.system.deleteBinLine
import com.lalilu.remixicon.system.menuSearchLine
import com.zhangke.krouter.annotation.Destination
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

@Destination("/pages/playlist/detail")
data class PlaylistDetailScreen(
    val playlistId: String
) : Screen, ScreenInfoFactory, ScreenActionFactory, ScreenBarFactory {

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = { stringResource(id = R.string.playlist_screen_detail) }
        )
    }

    @Composable
    override fun provideScreenActions(): List<ScreenAction> {
        val vm = screenVM<PlaylistDetailVM>(
            parameters = { parametersOf(playlistId) }
        )

        val state by vm.state

        return remember {
            listOf(
                ScreenAction.Static(
                    title = { "排序" },
                    icon = { RemixIcon.Editor.sortDesc },
                    color = { Color(0xFF1793FF) },
                    onAction = { vm.intent(PlaylistDetailAction.ToggleSortPanel) }
                ),
                ScreenAction.Static(
                    title = { "选择" },
                    icon = { RemixIcon.Design.editBoxLine },
                    color = { Color(0xFF009673) },
                    onAction = { vm.selector.isSelecting.value = true }
                ),
                ScreenAction.Dynamic {
                    val color = Color(0xFFF5381D)

                    LongClickableTextButton(
                        modifier = Modifier.fillMaxHeight(),
                        shape = RectangleShape,
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = color.copy(alpha = 0.15f),
                            contentColor = color
                        ),
                        enableLongClickMask = true,
                        onLongClick = {
                            val ids = vm.selector.selected().map { it.id }

                            vm.intent(PlaylistDetailAction.RemoveItems(ids))
                        },
                        onClick = { ToastUtils.showShort("请长按此按钮以继续") },
                    ) {
                        Image(
                            modifier = Modifier.size(20.dp),
                            imageVector = RemixIcon.System.deleteBinLine,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(color = color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "删除歌单",
                            fontSize = 14.sp
                        )
                    }
                },
                ScreenAction.Static(
                    title = { "搜索" },
                    subTitle = {
                        val keyword = state.searchKeyWord
                        if (keyword.isNotBlank()) "搜索中： $keyword" else null
                    },
                    icon = { RemixIcon.System.menuSearchLine },
                    color = { Color(0xFF8BC34A) },
                    dotColor = {
                        val keyword = state.searchKeyWord
                        if (keyword.isNotBlank()) Color.Red else null
                    },
                    onAction = {
                        vm.intent(PlaylistDetailAction.ToggleSearcherPanel)
                        DialogWrapper.dismiss()
                    }
                ),
                ScreenAction.Static(
                    title = { "定位至当前播放歌曲" },
                    icon = { RemixIcon.Design.focus3Line },
                    color = { Color(0xFF8700FF) },
                    onAction = { vm.intent(PlaylistDetailAction.LocaleToPlayingItem) }
                ),
            )
        }
    }

    @Composable
    override fun Content() {
        val vm = screenVM<PlaylistDetailVM>(
            parameters = { parametersOf(playlistId) }
        )

        val state by vm.state
        val songs by vm.songs
        val playlist by vm.playlist

        SongsSortPanelDialog(
            isVisible = { state.showSortPanel },
            onDismiss = { vm.intent(PlaylistDetailAction.HideSortPanel) },
            supportSortActions = vm.supportSortActions,
            isSortActionSelected = { state.selectedSortAction == it },
            onSelectSortAction = { vm.intent(PlaylistDetailAction.SelectSortAction(it)) }
        )

        SongsHeaderJumperDialog(
            isVisible = { state.showJumperDialog },
            onDismiss = { vm.intent(PlaylistDetailAction.HideJumperDialog) },
            items = { songs.keys },
            onSelectItem = { vm.intent(PlaylistDetailAction.LocaleToGroupItem(it)) }
        )

        SongsSearcherPanel(
            isVisible = { state.showSearcherPanel },
            onDismiss = { vm.intent(PlaylistDetailAction.HideSearcherPanel) },
            keyword = { state.searchKeyWord },
            onUpdateKeyword = { vm.intent(PlaylistDetailAction.SearchFor(it)) }
        )

        SongsSelectorPanel(
            isVisible = { vm.selector.isSelecting.value },
            onDismiss = { vm.selector.isSelecting.value = false },
            screenActions = listOfNotNull(
                ScreenAction.Static(
                    title = { "全选" },
                    color = { Color(0xFF00ACF0) },
                    icon = { RemixIcon.System.checkboxMultipleLine },
                    onAction = { vm.selector.selectAll(vm.songs.value.values.flatten()) }
                ),
                ScreenAction.Static(
                    title = { "取消全选" },
                    icon = { RemixIcon.System.checkboxMultipleBlankLine },
                    color = { Color(0xFFFF5100) },
                    onAction = { vm.selector.clear() }
                ),
                requestFor<ScreenAction>(
                    qualifier = named("add_to_favourite_action"),
                    parameters = { parametersOf(vm.selector::selected) }
                ),
                requestFor<ScreenAction>(
                    qualifier = named("add_to_playlist_action"),
                    parameters = { parametersOf(vm.selector::selected) }
                )
            )
        )

        PlaylistDetailScreenContent(
            songs = songs,
            playlist = playlist,
            enableDraggable = state.selectedSortAction is SortStaticAction.Normal,
            keys = { vm.recorder.list().filterNotNull() },
            recorder = vm.recorder,
            eventFlow = vm.eventFlow(),
            isSelecting = { vm.selector.isSelecting.value },
            isSelected = { vm.selector.isSelected(it) },
            onSelect = { vm.selector.onSelect(it) },
            onClickGroup = { vm.intent(PlaylistDetailAction.ToggleJumperDialog) },
            onUpdatePlaylist = { vm.intent(PlaylistDetailAction.UpdatePlaylist(it)) }
        )
    }
}