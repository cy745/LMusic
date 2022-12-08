package com.lalilu.lmusic.service.playback

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemotePlayer @Inject constructor(
    @ApplicationContext private val context: Context
) : Player, Player.Listener {
    override var listener: Player.Listener? = null
    override var isPrepared: Boolean = false
    override var isPlaying: Boolean = false
    override var isStopped: Boolean = true

    override fun load(uri: Uri, startWhenReady: Boolean) {
    }

    override fun play() {
    }

    override fun pause() {
    }

    override fun stop() {
    }

    override fun seekTo(durationMs: Number) {
    }

    override fun requestAudioFocus(): Boolean {
        return true
    }

    override fun getPosition(): Long {
        return 0L
    }

    override fun onLStart() {
        listener?.onLStart()
    }

    override fun onLStop() {
        listener?.onLStop()
    }

    override fun onLPlay() {
        listener?.onLPlay()
    }

    override fun onLPause() {
        listener?.onLPause()
    }

    override fun onLSeekTo(newDurationMs: Number) {
        listener?.onLSeekTo(newDurationMs)
    }

    override fun onLPrepared() {
        listener?.onLPrepared()
    }

    override fun onLCompletion() {
        listener?.onLCompletion()
    }
}