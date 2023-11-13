package com.lalilu.lmusic.compose

import android.app.Activity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.lalilu.lmusic.LMusicTheme
import com.lalilu.component.base.LocalWindowSize

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
object App {

    @Composable
    fun Content(activity: Activity) {
        Environment(activity = activity) {
            LayoutWrapper.Content()
        }
    }

    @Composable
    fun Environment(activity: Activity, content: @Composable () -> Unit) {
        LMusicTheme {
            CompositionLocalProvider(
                LocalWindowSize provides calculateWindowSizeClass(activity = activity),
                content = content
            )
        }
    }
}