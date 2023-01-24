package com.lalilu.lmusic.service.playback


interface Playback<T> {
    var playbackListener: Listener<T>?
    var queue: PlayQueue<T>?
    var player: Player?

    fun changeToPlayer(changeTo: Player)

    interface Listener<T> {
        fun onPlayingItemUpdate(item: T?)
        fun onPlaybackStateChanged(playbackState: Int, position: Long)
        fun onSetRepeatMode(repeatMode: Int)
        fun onSetShuffleMode(shuffleMode: Int)
    }
}