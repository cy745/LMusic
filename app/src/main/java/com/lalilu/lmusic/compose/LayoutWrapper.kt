package com.lalilu.lmusic.compose

import android.content.res.Configuration
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import com.lalilu.lmusic.compose.component.FixedLayout
import com.lalilu.lmusic.compose.screen.ShowScreen
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.rememberIsPad


object LayoutWrapper {

    @Composable
    fun Content(windowSize: WindowSizeClass = LocalWindowSize.current) {
        val configuration = LocalConfiguration.current
        val isPad by windowSize.rememberIsPad()
        val isLandscape by remember(configuration.orientation) {
            derivedStateOf { configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
        }

        DialogWrapper.Content {
            FixedLayout {
                NavigationWrapper.Content()
            }
        }

        if (isLandscape) {
            ShowScreen()
        }
    }
}