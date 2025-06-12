package com.lalilu.lmusic.compose.screen.home

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lalilu.common.ext.requestFor
import com.lalilu.component.LazyGridContent
import com.lalilu.component.base.LocalSmartBarPadding
import com.lalilu.component.divider
import com.lalilu.component.extension.fadeEdgeForStatusBar
import com.lalilu.lmusic.extension.DailyRecommend
import com.lalilu.lmusic.extension.EntryPanel
import com.lalilu.lmusic.extension.LatestPanel
import org.koin.core.qualifier.named

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
) {
    val padding by LocalSmartBarPadding.current
    val statusBar = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues()

    val dailyRecommend = DailyRecommend.register()
    val entryPanel = EntryPanel.register()
    val latestPanel = LatestPanel.register()
    val historyPanel = remember {
        requestFor<LazyGridContent>(named("history_panel"))
    }?.register()

    LazyVerticalGrid(
        modifier = modifier.fadeEdgeForStatusBar(),
        columns = GridCells.Fixed(12),
        contentPadding = PaddingValues(
            top = statusBar.calculateTopPadding(),
            bottom = padding.calculateBottomPadding()
        )
    ) {
        dailyRecommend(this)

        latestPanel(this)

        historyPanel?.invoke(this)

        entryPanel(this)

        divider {
            it.height(padding.calculateBottomPadding() + 16.dp)
        }
    }
}