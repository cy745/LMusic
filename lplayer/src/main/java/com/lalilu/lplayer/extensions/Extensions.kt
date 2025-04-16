package com.lalilu.lplayer.extensions

import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

fun MediaSessionCompat.isPlaying(): Boolean {
    return PlaybackStateCompat.STATE_PLAYING == controller.playbackState?.state
}
