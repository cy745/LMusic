package com.lalilu.lmusic.compose.screen.home

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lalilu.component.base.LocalSmartBarPadding
import com.lalilu.component.divider
import com.lalilu.lmusic.extension.DailyRecommend
import com.lalilu.lmusic.extension.EntryPanel
import com.lalilu.lmusic.extension.HistoryPanel
import com.lalilu.lmusic.extension.LatestPanel

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
) {
    val padding by LocalSmartBarPadding.current

    val dailyRecommend = DailyRecommend.register()
    val entryPanel = EntryPanel.register()
    val historyPanel = HistoryPanel.register()
    val latestPanel = LatestPanel.register()

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(12),
        contentPadding = WindowInsets.systemBars.asPaddingValues()
    ) {
        dailyRecommend(this)

        latestPanel(this)

        historyPanel(this)

        entryPanel(this)

        divider {
            it.height(padding.calculateBottomPadding() + 16.dp)
        }
    }
}