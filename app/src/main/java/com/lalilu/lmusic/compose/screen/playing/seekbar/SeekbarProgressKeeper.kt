package com.lalilu.lmusic.compose.screen.playing.seekbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

internal class SeekbarProgressKeeper(
    private val minValue: () -> Float,
    private val maxValue: () -> Float,
    private val sizeWidth: () -> Float,
    private val scrollSensitivity: Float,
) {
    var nowValue: Float by mutableFloatStateOf(0f)
        private set

    fun updateValue(value: Float) {
        nowValue = value.coerceIn(minValue(), maxValue())
    }

    fun updateValueByDelta(delta: Float) {
        val value = nowValue + delta / sizeWidth() * (maxValue() - minValue()) * scrollSensitivity
        updateValue(value)
    }
}

@Composable
internal fun rememberSeekbarProgressKeeper(
    minValue: () -> Float,
    maxValue: () -> Float,
    sizeWidth: () -> Float,
    scrollSensitivity: Float = 1f
): SeekbarProgressKeeper {
    return remember {
        SeekbarProgressKeeper(
            minValue = minValue,
            maxValue = maxValue,
            sizeWidth = sizeWidth,
            scrollSensitivity = scrollSensitivity
        )
    }
}