package com.lalilu.lartist.screen.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.model.rememberScreenModel
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
import com.lalilu.lartist.R
import com.lalilu.lartist.viewModel.ArtistDetailSM
import com.lalilu.lartist.viewModel.ArtistDetailScreenAction
import com.lalilu.lmedia.extension.GroupIdentity
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


@Destination("/pages/artist/detail")
data class ArtistDetailScreen(
    private val artistName: String
) : Screen, ScreenInfoFactory, ScreenActionFactory, ScreenBarFactory {
    override val key: ScreenKey = "${super.key}_$artistName"

    @Transient
    private var artistDetailSM: ArtistDetailSM? = null

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = { stringResource(id = R.string.artist_screen_detail) },
        )
    }

    @Composable
    override fun provideScreenActions(): List<ScreenAction> = remember {
        listOf(
            ScreenAction.Static(
                title = { "排序" },
                icon = { RemixIcon.Editor.sortDesc },
                color = { Color(0xFF1793FF) },
                onAction = { artistDetailSM?.showSortPanel?.value = true }
            ),
            ScreenAction.Static(
                title = { "选择" },
                icon = { RemixIcon.Design.editBoxLine },
                color = { Color(0xFF009673) },
                onAction = { artistDetailSM?.selector?.isSelecting?.value = true }
            ),
            ScreenAction.Static(
                title = { "搜索" },
                subTitle = {
                    val isSearching = artistDetailSM?.searcher?.isSearching

                    if (isSearching?.value == true) "搜索中： ${artistDetailSM?.searcher?.keywordState?.value}"
                    else null
                },
                icon = { RemixIcon.System.menuSearchLine },
                color = { Color(0xFF8BC34A) },
                dotColor = {
                    val isSearching = artistDetailSM?.searcher?.isSearching

                    if (isSearching?.value == true) Color.Red
                    else null
                },
                onAction = {
                    artistDetailSM?.showSearcherPanel?.value = true
                    DialogWrapper.dismiss()
                }
            ),
            ScreenAction.Static(
                title = { "定位至当前播放歌曲" },
                icon = { RemixIcon.Design.focus3Line },
                color = { Color(0xFF8700FF) },
                onAction = { artistDetailSM?.doAction(ArtistDetailScreenAction.LocaleToPlayingItem) }
            ),
        )
    }

    @Composable
    override fun Content() {
        val sm = rememberScreenModel { ArtistDetailSM(artistName) }
            .also { artistDetailSM = it }

        SongsSortPanelDialog(
            isVisible = sm.showSortPanel,
            supportSortActions = sm.supportSortActions,
            isSortActionSelected = { sm.sorter.isSortActionSelected(it) },
            onSelectSortAction = { sm.sorter.selectSortAction(it) }
        )

        SongsHeaderJumperDialog(
            isVisible = sm.showJumperDialog,
            items = { sm.recorder.list().filterIsInstance<GroupIdentity>() },
            onSelectItem = { sm.doAction(ArtistDetailScreenAction.LocaleToGroupItem(it)) }
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
                        val songs = sm.songs.value.values.flatten()
                        sm.selector.selectAll(songs)
                    }
                ),
                ScreenAction.Static(
                    title = { "取消全选" },
                    icon = { RemixIcon.System.checkboxMultipleBlankLine },
                    color = { Color(0xFFFF5100) },
                    onAction = { sm.selector.clear() }
                ),
                requestFor<ScreenAction>(
                    qualifier = named("add_to_favourite_action"),
                    parameters = { parametersOf(sm.selector::selected) }
                ),
                requestFor<ScreenAction>(
                    qualifier = named("add_to_playlist_action"),
                    parameters = { parametersOf(sm.selector::selected) }
                )
            )
        )

        ArtistDetailScreenContent(
            artistDetailSM = sm,
            isSelecting = { sm.selector.isSelecting.value },
            isSelected = { sm.selector.isSelected(it) },
            onSelect = { sm.selector.onSelect(it) },
            onClickGroup = { sm.showJumperDialog.value = true }
        )
    }
}

fun Long.durationToTime(): String {
    val hour = this / 3600000
    val minute = this / 60000 % 60
    val second = this / 1000 % 60
    return if (hour > 0L) "%02d:%02d:%02d".format(hour, minute, second)
    else "%02d:%02d".format(minute, second)
}