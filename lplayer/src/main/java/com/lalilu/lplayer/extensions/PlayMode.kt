package com.lalilu.lplayer.extensions

import androidx.media3.common.Player

enum class PlayMode(val index: Int) {
    ListRecycle(0),
    RepeatOne(1),
    Shuffle(2);

    companion object {
        fun indexOf(index: Int): PlayMode {
            return entries.firstOrNull { it.index == index } ?: ListRecycle
        }

        fun of(repeatMode: Int, shuffleModeEnabled: Boolean): PlayMode {
            if (repeatMode == Player.REPEAT_MODE_ONE) return RepeatOne
            if (shuffleModeEnabled) return Shuffle
            return ListRecycle
        }

        fun from(string: String?): PlayMode {
            return string?.let { valueOf(it) } ?: ListRecycle
        }
    }
}

var Player.playMode
    get() = PlayMode.of(repeatMode, shuffleModeEnabled)
    set(value) {
        shuffleModeEnabled = value == PlayMode.Shuffle
        repeatMode = if (value == PlayMode.RepeatOne) Player.REPEAT_MODE_ONE
        else Player.REPEAT_MODE_ALL
    }