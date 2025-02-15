package com.lalilu.lmusic.compose.screen.playing.lyric

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Constraints

/**
 * 歌词组件的上下文环境
 */
data class LyricContext(
    val currentTime: () -> Long,
    val currentIndex: () -> Int,
    val isUserScrolling: () -> Boolean,
    val screenConstraints: Constraints,
    val textMeasurer: TextMeasurer,
)