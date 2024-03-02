package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.lalilu.R
import com.lalilu.component.LLazyColumn
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.base.TabScreen
import com.lalilu.component.extension.singleViewModel
import com.lalilu.lmusic.extension.DailyRecommend
import com.lalilu.lmusic.extension.EntryPanel
import com.lalilu.lmusic.extension.HistoryPanel
import com.lalilu.lmusic.extension.LatestPanel
import com.lalilu.lmusic.viewmodel.LibraryViewModel

object HomeScreen : DynamicScreen(), TabScreen {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.screen_title_home,
        icon = R.drawable.ic_loader_line
    )

    @Composable
    override fun Content() {
        val vm: LibraryViewModel = singleViewModel()

        LaunchedEffect(Unit) {
            vm.checkOrUpdateToday()
        }

        LLazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = WindowInsets.statusBars.asPaddingValues()
        ) {
            item {
                DailyRecommend()
            }

            item {
                LatestPanel()
            }

            item {
                HistoryPanel()
            }

            item {
                EntryPanel()
            }
        }
    }
}