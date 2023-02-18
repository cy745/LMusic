package com.lalilu.lmusic.service.playback

interface Playback<T> {
    var playbackListener: Listener<T>?
    var queue: PlayQueue<T>?
    var player: Player?
    var playMode: PlayMode

    fun changeToPlayer(changeTo: Player)
    fun setMaxVolume(volume: Int)

    interface Listener<T> {
        fun onPlayInfoUpdate(item: T?, playbackState: Int, position: Long)
        fun onSetPlayMode(playMode: PlayMode)
    }
}