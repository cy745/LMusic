package com.lalilu.lmusic.compose.screen.songs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.R
import com.lalilu.component.base.ScreenAction
import com.lalilu.component.base.screen.ScreenActionFactory
import com.lalilu.component.base.screen.ScreenBarFactory
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.base.screen.ScreenType
import com.zhangke.krouter.annotation.Destination

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
            ScreenAction.StaticAction(
                title = R.string.screen_action_sort,
                icon = R.drawable.ic_sort_desc,
                color = Color(0xFF1793FF),
                onAction = { songsSM?.action(SongsScreenAction.ToggleSortPanel) }
            ),
            ScreenAction.StaticAction(
                title = R.string.screen_action_locate_playing_item,
                icon = R.drawable.ic_focus_3_line,
                color = Color(0xFF9317FF),
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
            onSelectSortAction = { sm.action(SongsScreenAction.SelectSortAction(it)) }
        )

        SongsScreenContent(songsSM = sm)
    }
}
