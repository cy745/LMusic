package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.component.TwoColumnWithPad
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.base.TabScreen
import com.lalilu.component.extension.singleViewModel
import com.lalilu.lmusic.extension.EntryPanel
import com.lalilu.lmusic.extension.dailyRecommend
import com.lalilu.lmusic.extension.dailyRecommendVertical
import com.lalilu.lmusic.extension.historyPanel
import com.lalilu.lmusic.extension.latestPanel
import com.lalilu.lmusic.viewmodel.HistoryViewModel
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel

object HomeScreen : DynamicScreen(), TabScreen {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.screen_title_home,
        icon = R.drawable.ic_loader_line
    )

    @Composable
    override fun Content() {
        val libraryVM: LibraryViewModel = singleViewModel()
        val historyVM: HistoryViewModel = singleViewModel()
        val playingVM: PlayingViewModel = singleViewModel()

        LaunchedEffect(Unit) {
            libraryVM.checkOrUpdateToday()
        }

        TwoColumnWithPad(
            arrangementForPad = Arrangement.spacedBy(10.dp),
            columnForPad = {
                dailyRecommendVertical(libraryVM = libraryVM)
            },
            columnForNormal = { isPad ->
                if (!isPad) {
                    dailyRecommend(
                        libraryVM = libraryVM,
                    )
                }

                latestPanel(
                    libraryVM = libraryVM,
                    playingVM = playingVM
                )

                historyPanel(
                    historyVM = historyVM,
                    playingVM = playingVM
                )

                item {
                    EntryPanel()
                }
            }
        )
    }
}
