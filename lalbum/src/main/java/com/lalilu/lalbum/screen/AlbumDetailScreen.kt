package com.lalilu.lalbum.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.lalilu.RemixIcon
import com.lalilu.common.ext.requestFor
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
import com.lalilu.component.extension.getViewModel
import com.lalilu.component.extension.registerAndGetViewModel
import com.lalilu.lalbum.R
import com.lalilu.lalbum.viewModel.AlbumDetailAction
import com.lalilu.lalbum.viewModel.AlbumDetailVM
import com.lalilu.remixicon.Design
import com.lalilu.remixicon.Editor
import com.lalilu.remixicon.System
import com.lalilu.remixicon.design.editBoxLine
import com.lalilu.remixicon.design.focus3Line
import com.lalilu.remixicon.editor.sortDesc
import com.lalilu.remixicon.system.checkboxMultipleBlankLine
import com.lalilu.remixicon.system.checkboxMultipleLine
import com.lalilu.remixicon.system.menuSearchLine
import com.zhangke.krouter.annotation.Destination
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

@Destination("/pages/albums/detail")
data class AlbumDetailScreen(
    private val albumId: String
) : Screen, ScreenInfoFactory, ScreenActionFactory, ScreenBarFactory {
    override val key: ScreenKey = "${super.key}:$albumId"

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = { stringResource(id = R.string.album_screen_title) }
        )
    }

    @Composable
    override fun provideScreenActions(): List<ScreenAction> {
        val vm = getViewModel<AlbumDetailVM>()
        val state by vm.state

        return remember {
            listOf(
                ScreenAction.Static(
                    title = { "排序" },
                    icon = { RemixIcon.Editor.sortDesc },
                    color = { Color(0xFF1793FF) },
                    onAction = { vm.intent(AlbumDetailAction.ToggleSortPanel) }
                ),
                ScreenAction.Static(
                    title = { "选择" },
                    icon = { RemixIcon.Design.editBoxLine },
                    color = { Color(0xFF009673) },
                    onAction = { vm.selector.isSelecting.value = true }
                ),
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
                        vm.intent(AlbumDetailAction.ToggleSearcherPanel)
                        DialogWrapper.dismiss()
                    }
                ),
                ScreenAction.Static(
                    title = { "定位至当前播放歌曲" },
                    icon = { RemixIcon.Design.focus3Line },
                    color = { Color(0xFF8700FF) },
                    onAction = { vm.intent(AlbumDetailAction.LocaleToPlayingItem) }
                ),
            )
        }
    }

    @Composable
    override fun Content() {
        val vm = registerAndGetViewModel<AlbumDetailVM>(parameters = { parametersOf(albumId) })
        val songs by vm.songs
        val state by vm.state
        val album by vm.album

        SongsSortPanelDialog(
            isVisible = { state.showSortPanel },
            onDismiss = { vm.intent(AlbumDetailAction.HideSortPanel) },
            supportSortActions = vm.supportSortActions,
            isSortActionSelected = { state.selectedSortAction == it },
            onSelectSortAction = { vm.intent(AlbumDetailAction.SelectSortAction(it)) }
        )

        SongsHeaderJumperDialog(
            isVisible = { state.showJumperDialog },
            onDismiss = { vm.intent(AlbumDetailAction.HideJumperDialog) },
            items = { songs.keys },
            onSelectItem = { vm.intent(AlbumDetailAction.LocaleToGroupItem(it)) }
        )

        SongsSearcherPanel(
            isVisible = { state.showSearcherPanel },
            onDismiss = { vm.intent(AlbumDetailAction.HideSearcherPanel) },
            keyword = { state.searchKeyWord },
            onUpdateKeyword = { vm.intent(AlbumDetailAction.SearchFor(it)) }
        )

        SongsSelectorPanel(
            isVisible = { vm.selector.isSelecting.value },
            onDismiss = { vm.selector.isSelecting.value = false },
            screenActions = listOfNotNull(
                ScreenAction.Static(
                    title = { "全选" },
                    color = { Color(0xFF00ACF0) },
                    icon = { RemixIcon.System.checkboxMultipleLine },
                    onAction = { vm.selector.selectAll(songs.values.flatten()) }
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

        AlbumDetailScreenContent(
            songs = songs,
            album = album,
            recorder = vm.recorder,
            eventFlow = vm.eventFlow(),
            keys = { vm.recorder.list().filterNotNull() },
            isSelecting = { vm.selector.isSelecting.value },
            isSelected = { vm.selector.isSelected(it) },
            onSelect = { vm.selector.onSelect(it) },
            onClickGroup = { vm.intent(AlbumDetailAction.ToggleJumperDialog) }
        )
    }
}