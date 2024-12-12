package com.lalilu.lplayer.extensions

import androidx.media3.common.Player

sealed interface PlayMode {
    data object ListRecycle : PlayMode
    data object RepeatOne : PlayMode
    data object Shuffle : PlayMode

    companion object {
        fun of(repeatMode: Int, shuffleModeEnabled: Boolean): PlayMode {
            if (repeatMode == Player.REPEAT_MODE_ONE) return RepeatOne
            if (shuffleModeEnabled) return Shuffle
            return ListRecycle
        }
    }
}

var Player.playMode
    get() = PlayMode.of(repeatMode, shuffleModeEnabled)
    set(value) {
        shuffleModeEnabled = value is PlayMode.Shuffle
        repeatMode = if (value is PlayMode.RepeatOne) Player.REPEAT_MODE_ONE
        else Player.REPEAT_MODE_ALL
    }