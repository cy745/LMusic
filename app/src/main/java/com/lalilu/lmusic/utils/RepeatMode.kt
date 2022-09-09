package com.lalilu.lmusic.utils

import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE

enum class RepeatMode(
    val repeatMode: Int,
    val isShuffle: Boolean
) {
    ListRecycle(repeatMode = REPEAT_MODE_ALL, isShuffle = false),
    RepeatOne(repeatMode = REPEAT_MODE_ONE, isShuffle = false),
    Shuffle(repeatMode = REPEAT_MODE_ALL, isShuffle = true);

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
            if (repeatMode == REPEAT_MODE_ONE) return RepeatOne
            return ListRecycle
        }
    }
}