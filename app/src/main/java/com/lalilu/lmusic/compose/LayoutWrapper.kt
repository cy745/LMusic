package com.lalilu.lmusic.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.rememberIsPad

@OptIn(ExperimentalFoundationApi::class)
object LayoutWrapper {
    val enableUserScroll = mutableStateOf(true)

    @Composable
    fun Content(
        windowSize: WindowSizeClass = LocalWindowSize.current,
        playingContent: @Composable () -> Unit,
        libraryContent: @Composable () -> Unit,
    ) {
        val isPad by windowSize.rememberIsPad()
        val pagerState = rememberPagerState(initialPage = 0)

        if (isPad) {
            DrawerWrapper.Content(
                mainContent = { playingContent() },
                secondContent = { libraryContent() }
            )
        } else {
            HorizontalPager(
                state = pagerState,
                pageCount = 2,
                beyondBoundsPageCount = 2,
                userScrollEnabled = enableUserScroll.value
            ) {
                if (it == 0) playingContent() else libraryContent()
            }
        }
    }
}