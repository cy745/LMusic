package com.lalilu.lmusic.compose

import android.content.res.Configuration
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.component.extension.DynamicTipsHost
import com.lalilu.lmusic.compose.screen.ShowScreen
import com.lalilu.lmusic.compose.screen.playing.PlayingLayout

object LayoutWrapper {

    @Composable
    fun BoxScope.Content() {
        val configuration = LocalConfiguration.current
        val isLandscape by remember(configuration.orientation) {
            derivedStateOf { configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
        }

        NavigationWrapper.Content { PlayingLayout() }

        if (isLandscape) {
            ShowScreen()
        }

        DialogWrapper.Content()

        with(DynamicTipsHost) { Content() }
    }
}