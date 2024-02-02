package com.lalilu.lplayer.playback

import android.support.v4.media.session.MediaSessionCompat
import com.lalilu.lplayer.extensions.AudioFocusHelper
import com.lalilu.lplayer.extensions.PlayerAction

abstract class Playback<T> : MediaSessionCompat.Callback() {
    abstract var audioFocusHelper: AudioFocusHelper?
    abstract var playbackListener: Listener<T>?
    abstract var queue: UpdatableQueue<T>?
    abstract var player: Player?
    abstract var playMode: PlayMode

    abstract fun pauseWhenCompletion()
    abstract fun cancelPauseWhenCompletion()

    abstract fun readyToUse(): Boolean
    abstract fun changeToPlayer(changeTo: Player)
    abstract fun setMaxVolume(volume: Int)
    abstract fun preloadNextItem()
    abstract fun destroy()
    abstract fun handleCustomAction(action: PlayerAction.CustomAction)

    interface Listener<T> {
        fun onPlayInfoUpdate(item: T?, playbackState: Int, position: Long)
        fun onSetPlayMode(playMode: PlayMode)
        fun onItemPlay(item: T)
        fun onItemPause(item: T)
        fun onPlayerCreated(id: Any)
    }
}