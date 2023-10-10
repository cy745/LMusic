package com.lalilu.lplayer.playback

import com.lalilu.lplayer.extensions.PlayerAction

interface Playback<T> {
    var playbackListener: Listener<T>?
    var queue: UpdatableQueue<T>?
    var player: Player?
    var playMode: PlayMode

    fun readyToUse(): Boolean
    fun changeToPlayer(changeTo: Player)
    fun setMaxVolume(volume: Int)
    fun preloadNextItem()
    fun destroy()
    fun handleCustomAction(action: PlayerAction.CustomAction)

    interface Listener<T> {
        fun onPlayInfoUpdate(item: T?, playbackState: Int, position: Long)
        fun onSetPlayMode(playMode: PlayMode)
        fun onItemPlay(item: T)
        fun onItemPause(item: T)
        fun onPlayerCreated(id: Any)
    }
}