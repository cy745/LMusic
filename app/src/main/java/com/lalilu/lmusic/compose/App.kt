package com.lalilu.lmusic.compose

import android.app.Activity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.rememberNavController
import com.lalilu.lmusic.LMusicTheme
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.LocalWindowSize

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
object App {

    @Composable
    fun Content(activity: Activity) {
        Environment(activity = activity) {
            LayoutWrapper.Content(
                playingContent = { Playing.Content() },
                libraryContent = { Library.Content() }
            )
        }
    }

    @Composable
    fun Environment(activity: Activity, content: @Composable () -> Unit) {
        LMusicTheme {
            CompositionLocalProvider(
                LocalWindowSize provides calculateWindowSizeClass(activity = activity),
                LocalNavigatorHost provides rememberNavController(),
                content = content
            )
        }
    }
}