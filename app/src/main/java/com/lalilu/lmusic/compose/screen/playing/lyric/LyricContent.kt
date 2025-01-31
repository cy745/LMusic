package com.lalilu.lmusic.compose.screen.playing.lyric

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Constraints
import com.lalilu.lmedia.lyric.LyricItem


interface LyricContent {
    val key: String
    val item: LyricItem

    /**
     * 歌词元素绘制
     *
     * @param offsetToCurrent   此元素在列表中距离当前播放元素的偏移量
     * @param screenConstraints       屏幕的边界约束（预测量用）
     */
    @Composable
    fun Draw(
        modifier: Modifier,
        isCurrent: () -> Boolean,
        offsetToCurrent: () -> Int,
        currentTime: () -> Long,
        screenConstraints: Constraints,
        onClick: (() -> Unit)? = null,
        onLongClick: (() -> Unit)? = null,
        textMeasurer: TextMeasurer,
        fontFamily: () -> FontFamily? = { null },
    )
}

