package com.lalilu.lartist.screen.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.RemixIcon
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
import com.lalilu.lartist.R
import com.lalilu.lartist.viewModel.ArtistsSM
import com.lalilu.lartist.viewModel.ArtistsScreenAction
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.remixicon.Design
import com.lalilu.remixicon.Editor
import com.lalilu.remixicon.System
import com.lalilu.remixicon.UserAndFaces
import com.lalilu.remixicon.design.editBoxLine
import com.lalilu.remixicon.design.focus3Line
import com.lalilu.remixicon.editor.sortDesc
import com.lalilu.remixicon.system.checkboxMultipleBlankLine
import com.lalilu.remixicon.system.checkboxMultipleLine
import com.lalilu.remixicon.system.menuSearchLine
import com.lalilu.remixicon.userandfaces.userLine
import com.zhangke.krouter.annotation.Destination

@Destination("/pages/artists")
object ArtistsScreen : Screen, ScreenInfoFactory, ScreenActionFactory, ScreenBarFactory {
    private fun readResolve(): Any = ArtistsScreen
    private var artistsSM: ArtistsSM? = null

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = { stringResource(id = R.string.artist_screen_title) },
            icon = RemixIcon.UserAndFaces.userLine
        )
    }

    @Composable
    override fun provideScreenActions(): List<ScreenAction> {
        return remember {
            listOf(
                ScreenAction.Static(
                    title = { "排序" },
                    icon = { RemixIcon.Editor.sortDesc },
                    color = { Color(0xFF1793FF) },
                    onAction = { artistsSM?.showSortPanel?.value = true }
                ),
                ScreenAction.Static(
                    title = { "选择" },
                    icon = { RemixIcon.Design.editBoxLine },
                    color = { Color(0xFF009673) },
                    onAction = { artistsSM?.selector?.isSelecting?.value = true }
                ),
                ScreenAction.Static(
                    title = { "搜索" },
                    subTitle = {
                        val isSearching = artistsSM?.searcher?.isSearching

                        if (isSearching?.value == true) "搜索中： ${artistsSM?.searcher?.keywordState?.value}"
                        else null
                    },
                    icon = { RemixIcon.System.menuSearchLine },
                    color = { Color(0xFF8BC34A) },
                    dotColor = {
                        val isSearching = artistsSM?.searcher?.isSearching

                        if (isSearching?.value == true) Color.Red
                        else null
                    },
                    onAction = {
                        artistsSM?.showSearcherPanel?.value = true
                        DialogWrapper.dismiss()
                    }
                ),
                ScreenAction.Static(
                    title = { "定位当前播放所属" },
                    icon = { RemixIcon.Design.focus3Line },
                    color = { Color(0xFF8700FF) },
                    onAction = { artistsSM?.doAction(ArtistsScreenAction.LocaleToPlayingItem) }
                ),
            )
        }
    }

    @Composable
    override fun Content() {
        val sm = rememberScreenModel { ArtistsSM() }
            .also { artistsSM = it }

        SongsSortPanelDialog(
            isVisible = sm.showSortPanel,
            supportSortActions = sm.supportSortActions,
            isSortActionSelected = { sm.sorter.isSortActionSelected(it) },
            onSelectSortAction = { sm.sorter.selectSortAction(it) }
        )

        SongsHeaderJumperDialog(
            isVisible = sm.showJumperDialog,
            items = { sm.recorder.list().filterIsInstance<GroupIdentity>() },
            onSelectItem = { sm.doAction(ArtistsScreenAction.LocaleToGroupItem(it)) }
        )

        SongsSearcherPanel(
            isVisible = sm.showSearcherPanel,
            keyword = { sm.searcher.keywordState.value },
            onUpdateKeyword = { sm.searcher.keywordState.value = it }
        )

        SongsSelectorPanel(
            isVisible = sm.selector.isSelecting,
            screenActions = listOfNotNull(
                ScreenAction.Static(
                    title = { "全选" },
                    color = { Color(0xFF00ACF0) },
                    icon = { RemixIcon.System.checkboxMultipleLine },
                    onAction = {
                        val artists = sm.artists.value.values.flatten()
                        sm.selector.selectAll(artists)
                    }
                ),
                ScreenAction.Static(
                    title = { "取消全选" },
                    icon = { RemixIcon.System.checkboxMultipleBlankLine },
                    color = { Color(0xFFFF5100) },
                    onAction = { sm.selector.clear() }
                ),
                ScreenAction.Static(
                    title = { "添加到播放列表" },
                    icon = { RemixIcon.System.checkboxMultipleBlankLine },
                    color = { Color(0xFF002FB9) },
                    onAction = {
                        // TODO 选择歌手后将其列表下歌曲添加到播放列表
                        ToastUtils.showShort("开发中，敬请期待")
                    }
                ),
            )
        )

        ArtistsScreenContent(
            artistsSM = sm,
            isSelecting = { sm.selector.isSelecting.value },
            isSelected = { sm.selector.isSelected(it) },
            onSelect = { sm.selector.onSelect(it) },
            onClickGroup = { sm.showJumperDialog.value = true }
        )
    }
}