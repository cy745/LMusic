package com.lalilu.lmusic.compose.component.playing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.lalilu.lmusic.utils.extension.rememberFixedStatusBarHeightDp


@Composable
fun PlayingToolbarScaffold(
    state: PlayingToolbarScaffoldState = rememberPlayingToolbarScaffoldState(),
    topContent: @Composable ColumnScope.() -> Unit,
    bottomContent: @Composable ColumnScope.() -> Unit
) {
    val showTop by remember { derivedStateOf { state.minProgress.floatValue != 1f } }
    val showBottom by remember { derivedStateOf { state.maxProgress.floatValue != 0f } }
    val statusBarHeight = rememberFixedStatusBarHeightDp()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (showTop) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(
                        top = statusBarHeight,
                        bottom = 12.dp
                    )
                    .graphicsLayer { alpha = 1f - state.minProgress.floatValue },
                content = topContent,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            )
        }

        if (showBottom) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .graphicsLayer { alpha = state.maxProgress.floatValue },
                content = bottomContent,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            )
        }
    }
}

class PlayingToolbarScaffoldState {
    val minProgress: MutableFloatState = mutableFloatStateOf(1f)
    val maxProgress: MutableFloatState = mutableFloatStateOf(0f)

    fun updateProgress(
        minProgress: Float = this.minProgress.floatValue,
        maxProgress: Float = this.maxProgress.floatValue
    ) {
        this.minProgress.floatValue = minProgress
        this.maxProgress.floatValue = maxProgress
    }
}

@Composable
fun rememberPlayingToolbarScaffoldState(): PlayingToolbarScaffoldState {
    return remember { PlayingToolbarScaffoldState() }
}