package com.lalilu.lmusic.compose

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.rememberIsPad

object LayoutWrapper {

    @Composable
    fun Content(
        windowSize: WindowSizeClass = LocalWindowSize.current,
        playingContent: @Composable () -> Unit,
        libraryContent: @Composable () -> Unit,
    ) {
        val isPad by windowSize.rememberIsPad()

        if (isPad) {
            DrawerWrapper.Content(
                mainContent = playingContent,
                secondContent = libraryContent
            )
        } else {
            PagerWrapper.Content(
                mainContent = playingContent,
                secondContent = libraryContent
            )
        }
    }
}