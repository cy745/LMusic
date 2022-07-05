package com.lalilu.lmusic.utils

import androidx.media3.common.Player

enum class RepeatMode(
    val repeatMode: Int,
    val isShuffle: Boolean
) {
    ListRecycle(repeatMode = Player.REPEAT_MODE_ALL, isShuffle = false),
    RepeatOne(repeatMode = Player.REPEAT_MODE_ONE, isShuffle = false),
    Shuffle(repeatMode = Player.REPEAT_MODE_ALL, isShuffle = true);

    fun next(): RepeatMode {
        return when (this) {
            ListRecycle -> RepeatOne
            RepeatOne -> Shuffle
            Shuffle -> ListRecycle
        }
    }

    companion object {
        fun of(repeatMode: Int, isShuffle: Boolean): RepeatMode {
            if (isShuffle) return Shuffle
            if (repeatMode == Player.REPEAT_MODE_ONE) return RepeatOne
            return ListRecycle
        }
    }
}