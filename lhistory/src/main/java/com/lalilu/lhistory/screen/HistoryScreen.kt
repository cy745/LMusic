package com.lalilu.lhistory.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.RemixIcon
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.remixicon.Editor
import com.lalilu.remixicon.editor.draggable

data object HistoryScreen : Screen, ScreenInfoFactory {
    private fun readResolve(): Any = HistoryScreen

    @Composable
    override fun provideScreenInfo(): ScreenInfo {
        return remember {
            ScreenInfo(
                title = { "History" },
                icon = RemixIcon.Editor.draggable
            )
        }
    }

    @Composable
    override fun Content() {
        HistoryScreenContent()
    }
}

@Composable
private fun HistoryScreenContent(
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

    }
}