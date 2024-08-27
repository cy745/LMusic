package com.lalilu.lmusic.compose.screen.songs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.model.rememberScreenModel
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
import com.lalilu.remixicon.Design
import com.lalilu.remixicon.Editor
import com.lalilu.remixicon.design.editBoxLine
import com.lalilu.remixicon.design.focus3Line
import com.lalilu.remixicon.editor.sortDesc
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
            title = R.string.screen_title_songs,
            icon = R.drawable.ic_music_2_line
        )
    }

    @Composable
    override fun provideScreenActions(): List<ScreenAction> = remember {
        listOf(
            ScreenAction.Static(
                title = { stringResource(id = R.string.screen_action_sort) },
                icon = { RemixIcon.Editor.sortDesc },
                color = { Color(0xFF1793FF) },
                onAction = { songsSM?.action(SongsScreenAction.ToggleSortPanel) }
            ),
            ScreenAction.Static(
                title = { "选择" },
                icon = { RemixIcon.Design.editBoxLine },
                color = { Color(0xFF009673) },
                onAction = { songsSM?.selector?.isSelecting?.value = true }
            ),
            ScreenAction.Static(
                title = { stringResource(id = R.string.screen_action_locate_playing_item) },
                icon = { RemixIcon.Design.focus3Line },
                color = { Color(0xFF8700FF) },
                onAction = { songsSM?.action(SongsScreenAction.LocaleToPlayingItem) }
            ),
        )
    }

    @Transient
    private var songsSM: SongsSM? = null

    @Composable
    override fun Content() {
        val sm = rememberScreenModel { SongsSM(mediaIds) }
            .also { songsSM = it }

        SongsSortPanelDialog(
            isVisible = sm.showSortPanel,
            supportSortActions = sm.supportSortActions,
            isSortActionSelected = { sm.sorter.isSortActionSelected(it) },
            onSelectSortAction = { sm.sorter.selectSortAction(it) }
        )

        SongsSelectorPanel(
            isVisible = sm.selector.isSelecting,
            screenActions = listOfNotNull(
                ScreenAction.Static(
                    title = { "全选" },
                    onAction = {
                        val songs = sm.songs.value.values.flatten()
                        sm.selector.selectAll(songs)
                    }
                ),
                ScreenAction.Static(
                    title = { "取消全选" },
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

        SongsScreenContent(
            songsSM = sm,
            isSelecting = { sm.selector.isSelecting.value },
            isSelected = { sm.selector.isSelected(it) },
            onSelect = { sm.selector.onSelect(it) }
        )
    }
}
