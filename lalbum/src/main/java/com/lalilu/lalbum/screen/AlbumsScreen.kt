package com.lalilu.lalbum.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.RemixIcon
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.component.base.screen.ScreenActionFactory
import com.lalilu.component.base.screen.ScreenBarFactory
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.base.songs.SongsSearcherPanel
import com.lalilu.component.base.songs.SongsSortPanelDialog
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.component.extension.screenVM
import com.lalilu.lalbum.R
import com.lalilu.lalbum.viewModel.AlbumsAction
import com.lalilu.lalbum.viewModel.AlbumsVM
import com.lalilu.remixicon.Editor
import com.lalilu.remixicon.Media
import com.lalilu.remixicon.System
import com.lalilu.remixicon.editor.formatClear
import com.lalilu.remixicon.editor.sortDesc
import com.lalilu.remixicon.editor.text
import com.lalilu.remixicon.media.albumFill
import com.lalilu.remixicon.system.menuSearchLine
import com.zhangke.krouter.annotation.Destination

@Destination("/pages/albums")
data class AlbumsScreen(
    val albumsId: List<String> = emptyList()
) : Screen, ScreenInfoFactory, ScreenActionFactory, ScreenBarFactory {
    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = { stringResource(id = R.string.album_screen_title) },
            icon = RemixIcon.Media.albumFill
        )
    }

    @Composable
    override fun provideScreenActions(): List<ScreenAction> {
        val albumsVM = screenVM<AlbumsVM>()
        val state by albumsVM.state

        return remember {
            listOf(
                ScreenAction.Static(
                    title = { if (state.showText) "隐藏专辑名" else "显示专辑名" },
                    color = { Color(0xFF6E4AC3) },
                    icon = { if (state.showText) RemixIcon.Editor.text else RemixIcon.Editor.formatClear },
                    onAction = { albumsVM.intent(AlbumsAction.ToggleShowText) }
                ),
                ScreenAction.Static(
                    title = { "排序" },
                    icon = { RemixIcon.Editor.sortDesc },
                    color = { Color(0xFF1793FF) },
                    onAction = { albumsVM.intent(AlbumsAction.ToggleSortPanel) }
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
                        albumsVM.intent(AlbumsAction.ToggleSearcherPanel)
                        DialogWrapper.dismiss()
                    }
                ),
            )
        }
    }

    @Composable
    override fun Content() {
        val vm = screenVM<AlbumsVM>()
        val state by vm.state
        val albums by vm.albums

        SongsSortPanelDialog(
            isVisible = { state.showSortPanel },
            onDismiss = { vm.intent(AlbumsAction.HideSortPanel) },
            supportSortActions = vm.supportSortActions,
            isSortActionSelected = { state.selectedSortAction == it },
            onSelectSortAction = { vm.intent(AlbumsAction.SelectSortAction(it)) }
        )

        SongsSearcherPanel(
            isVisible = { state.showSearcherPanel },
            onDismiss = { vm.intent(AlbumsAction.HideSearcherPanel) },
            keyword = { state.searchKeyWord },
            onUpdateKeyword = { vm.intent(AlbumsAction.SearchFor(it)) }
        )

        AlbumsScreenContent(
            eventFlow = vm.eventFlow(),
            title = { "全部专辑" },
            albums = { albums },
            showText = { state.showText }
        )
    }
}