package com.lalilu.lmusic.compose.screen.songs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.R
import com.lalilu.RemixIcon
import com.lalilu.common.ext.requestFor
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.component.base.screen.ScreenActionFactory
import com.lalilu.component.base.screen.ScreenBarFactory
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.base.screen.ScreenType
import com.lalilu.component.base.songs.SongsHeaderJumperDialog
import com.lalilu.component.base.songs.SongsSearcherPanel
import com.lalilu.component.base.songs.SongsSelectorPanel
import com.lalilu.component.base.songs.SongsSortPanelDialog
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.component.extension.getViewModel
import com.lalilu.component.extension.registerAndGetViewModel
import com.lalilu.lmusic.viewmodel.SongsAction
import com.lalilu.lmusic.viewmodel.SongsVM
import com.lalilu.remixicon.Design
import com.lalilu.remixicon.Editor
import com.lalilu.remixicon.Media
import com.lalilu.remixicon.System
import com.lalilu.remixicon.design.editBoxLine
import com.lalilu.remixicon.design.focus3Line
import com.lalilu.remixicon.editor.sortDesc
import com.lalilu.remixicon.media.music2Line
import com.lalilu.remixicon.system.checkboxMultipleBlankLine
import com.lalilu.remixicon.system.checkboxMultipleLine
import com.lalilu.remixicon.system.menuSearchLine
import com.zhangke.krouter.annotation.Destination
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

@Destination("/pages/songs")
data class SongsScreen(
    private val title: String? = null,
    private val mediaIds: List<String> = emptyList()
) : Screen, ScreenInfoFactory, ScreenActionFactory, ScreenBarFactory, ScreenType.List {

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = { stringResource(id = R.string.screen_title_songs) },
            icon = RemixIcon.Media.music2Line,
        )
    }

    @Composable
    override fun provideScreenActions(): List<ScreenAction> {
        val vm = getViewModel<SongsVM>()
        val state by vm.state

        return remember {
            listOf(
                ScreenAction.Static(
                    title = { stringResource(id = R.string.screen_action_sort) },
                    icon = { RemixIcon.Editor.sortDesc },
                    color = { Color(0xFF1793FF) },
                    onAction = { vm.intent(SongsAction.ToggleSortPanel) }
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
                        vm.intent(SongsAction.ToggleSearcherPanel)
                        DialogWrapper.dismiss()
                    }
                ),
                ScreenAction.Static(
                    title = { stringResource(id = R.string.screen_action_locate_playing_item) },
                    icon = { RemixIcon.Design.focus3Line },
                    color = { Color(0xFF8700FF) },
                    onAction = { vm.intent(SongsAction.LocaleToPlayingItem) }
                ),
            )
        }
    }

    @Composable
    override fun Content() {
        val vm = registerAndGetViewModel<SongsVM>(parameters = { parametersOf(mediaIds) })
        val songs by vm.songs
        val state by vm.state

        SongsSortPanelDialog(
            isVisible = { state.showSortPanel },
            onDismiss = { vm.intent(SongsAction.HideSortPanel) },
            supportSortActions = vm.supportSortActions,
            isSortActionSelected = { state.selectedSortAction == it },
            onSelectSortAction = { vm.intent(SongsAction.SelectSortAction(it)) }
        )

        SongsHeaderJumperDialog(
            isVisible = { state.showJumperDialog },
            onDismiss = { vm.intent(SongsAction.HideJumperDialog) },
            items = { songs.keys },
            onSelectItem = { vm.intent(SongsAction.LocaleToGroupItem(it)) }
        )

        SongsSearcherPanel(
            isVisible = { state.showSearcherPanel },
            onDismiss = { vm.intent(SongsAction.HideSearcherPanel) },
            keyword = { state.searchKeyWord },
            onUpdateKeyword = { vm.intent(SongsAction.SearchFor(it)) }
        )

        SongsSelectorPanel(
            isVisible = { vm.selector.isSelecting.value },
            onDismiss = { vm.selector.isSelecting.value = false },
            screenActions = listOfNotNull(
                ScreenAction.Static(
                    title = { "全选" },
                    color = { Color(0xFF00ACF0) },
                    icon = { RemixIcon.System.checkboxMultipleLine },
                    onAction = {
                        val list = songs.values.flatten()
                        vm.selector.selectAll(list)
                    }
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

        SongsScreenContent(
            songs = songs,
            recorder = vm.recorder,
            eventFlow = vm.eventFlow(),
            keys = { vm.recorder.list().filterNotNull() },
            isSelecting = { vm.selector.isSelecting.value },
            isSelected = { vm.selector.isSelected(it) },
            onSelect = { vm.selector.onSelect(it) },
            onClickGroup = { vm.intent(SongsAction.ToggleJumperDialog) }
        )
    }
}
