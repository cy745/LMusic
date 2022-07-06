package com.lalilu.lmusic.utils

import com.lalilu.ui.CLICK_PART_LEFT
import com.lalilu.ui.CLICK_PART_MIDDLE
import com.lalilu.ui.CLICK_PART_RIGHT
import com.lalilu.ui.ClickPart

class SeekBarHandler(
    val onPlayNext: () -> Unit,
    val onPlayPrevious: () -> Unit,
    val onPlayPause: () -> Unit,
) {

    companion object {
        const val CLICK_HANDLE_MODE_CLICK = 0
        const val CLICK_HANDLE_MODE_DOUBLE_CLICK = 1
        const val CLICK_HANDLE_MODE_LONG_CLICK = 2
    }

    var clickHandleMode: Int = CLICK_HANDLE_MODE_CLICK

    fun handle(@ClickPart clickPart: Int) {
        when (clickPart) {
            CLICK_PART_LEFT -> onPlayPrevious
            CLICK_PART_MIDDLE -> onPlayPause
            CLICK_PART_RIGHT -> onPlayNext
            else -> null
        }?.invoke()
    }
}