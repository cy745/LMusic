package com.lalilu.lmusic.utils

import android.os.SystemClock
import android.support.v4.media.session.PlaybackStateCompat

object Mathf {
    fun <T : Number> clampInLoop(min: T, max: T, num: T): T {
        if (num.toDouble() < min.toDouble()) return max
        if (num.toDouble() > max.toDouble()) return min
        return num
    }

    fun getPositionFromPlaybackStateCompat(playbackStateCompat: PlaybackStateCompat): Long {
        return playbackStateCompat.position +
                (playbackStateCompat.playbackSpeed *
                        (SystemClock.elapsedRealtime() - playbackStateCompat.lastPositionUpdateTime)).toLong()
    }
}