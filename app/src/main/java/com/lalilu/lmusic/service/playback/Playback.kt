package com.lalilu.lmusic.service.playback

interface Playback<T> {
    var playbackListener: Listener<T>?
    var queue: PlayQueue<T>?
    var player: Player?
    var playMode: PlayMode

    fun changeToPlayer(changeTo: Player)
    fun setMaxVolume(volume: Int)
    fun onCustomActionIn(action: PlaybackAction?)
    fun handleCustomAction(action: String?) {
        onCustomActionIn(PlaybackAction.of(action))
    }

    fun destroy()

    interface Listener<T> {
        fun onPlayInfoUpdate(item: T?, playbackState: Int, position: Long)
        fun onSetPlayMode(playMode: PlayMode)
        fun onItemPlay(item: T)
    }

    enum class PlaybackAction {
        PlayPause, ReloadAndPlay;

        companion object {
            fun of(name: String?): PlaybackAction? = values().firstOrNull { it.name == name }
        }
    }
}