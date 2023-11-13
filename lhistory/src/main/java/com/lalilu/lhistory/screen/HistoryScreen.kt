package com.lalilu.lhistory.screen

import androidx.compose.runtime.Composable
import com.lalilu.component.Songs
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.ScreenInfo
import com.lalilu.lhistory.R
import com.lalilu.component.R as ComponentR

data object HistoryScreen : DynamicScreen() {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.history_screen_title,
        icon = ComponentR.drawable.ic_play_list_fill
    )

    @Composable
    override fun Content() {
        HistoryScreen()
    }
}

@Composable
private fun DynamicScreen.HistoryScreen() {
    Songs(
        mediaIds = emptyList(),
        supportListAction = { listOf() },
        headerContent = {

        },
        footerContent = {}
    )
}