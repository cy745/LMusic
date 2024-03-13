package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.lalilu.R
import com.lalilu.component.LLazyColumn
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.base.TabScreen
import com.lalilu.component.extension.singleViewModel
import com.lalilu.lmusic.extension.EntryPanel
import com.lalilu.lmusic.extension.dailyRecommend
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

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content() {
        val vm: LibraryViewModel = singleViewModel()
        val historyVM: HistoryViewModel = singleViewModel()
        val playingVM: PlayingViewModel = singleViewModel()

        LaunchedEffect(Unit) {
            vm.checkOrUpdateToday()
        }

        LLazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues()
        ) {
            dailyRecommend(
                libraryVM = vm,
            )

            latestPanel(
                libraryVM = vm,
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
    }
}