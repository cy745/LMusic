package com.lalilu.lmusic.compose

import android.content.res.Configuration
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import com.lalilu.lmusic.compose.component.FixedLayout
import com.lalilu.lmusic.compose.new_screen.HomeScreen
import com.lalilu.lmusic.compose.screen.ShowScreen
import com.lalilu.component.base.LocalWindowSize
import com.lalilu.component.extension.rememberIsPad


object LayoutWrapper {

    @Composable
    fun Content(windowSize: WindowSizeClass = LocalWindowSize.current) {
        val configuration = LocalConfiguration.current
        val isPad by windowSize.rememberIsPad()
        val isLandscape by remember(configuration.orientation) {
            derivedStateOf { configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
        }

        DialogWrapper.Content {
            if (isPad) {
                DrawerWrapper.Content(
                    mainContent = { Playing.Content() },
                    secondContent = { HomeScreen.Content() }
                )
            } else {
                FixedLayout {
                    NavigationWrapper.Content { Playing.Content() }
                }
            }
        }

        if (isLandscape) {
            ShowScreen()
        }
    }
}