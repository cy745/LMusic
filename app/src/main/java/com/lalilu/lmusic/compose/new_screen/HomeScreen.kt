package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.component.LLazyColumn
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

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content() {
        val density = LocalDensity.current
        val vm: LibraryViewModel = singleViewModel()
        val historyVM: HistoryViewModel = singleViewModel()
        val playingVM: PlayingViewModel = singleViewModel()
        val paddingTop = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues()
            .calculateTopPadding()

        LaunchedEffect(Unit) {
            vm.checkOrUpdateToday()
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val expended = with(density) { constraints.maxWidth.toDp() } > 500.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (expended) {
                    LLazyColumn(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentWidth(),
                        contentPadding = PaddingValues(top = paddingTop, start = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        dailyRecommendVertical(libraryVM = vm)
                    }
                }

                LLazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(top = paddingTop)
                ) {
                    if (!expended) {
                        dailyRecommend(
                            libraryVM = vm,
                        )
                    }

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
    }
}