package com.lalilu.lmusic.service.playback

import android.support.v4.media.MediaMetadataCompat

interface Playback<T> {
    var listener: Listener<T>?
    var queue: PlayQueue<T>?
    val player: Player

    interface Listener<T> {
        fun onPlayingItemUpdate(item: T?)
        fun onMetadataChanged(metadata: MediaMetadataCompat?)
        fun onPlaybackStateChanged(playbackState: Int, position: Long)
        fun onSetRepeatMode(repeatMode: Int)
        fun onSetShuffleMode(shuffleMode: Int)
    }
}