package com.lalilu.lmusic.service

import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import com.lalilu.lmusic.Config.ACTION_PLAY_PAUSE
import com.lalilu.lmusic.service.playback.MSongPlaybackFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class MSongSessionCallback
@Inject constructor(
    val playback: MSongPlaybackFlow,
    private val mediaSession: MediaSessionCompat,
) : MediaSessionCompat.Callback() {

    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
        playback.playByMediaId(mediaId)
    }

    override fun onPause() {
        playback.pause()
    }

    override fun onPlay() {
        playback.play()
    }

    override fun onSkipToNext() {
        playback.next()
    }

    override fun onSkipToPrevious() {
        playback.previous()
    }

    override fun onSeekTo(pos: Long) {
        playback.seekTo(pos)
    }

    override fun onStop() {
        mediaSession.isActive = false
        playback.stop()
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
        when (action) {
            ACTION_PLAY_PAUSE -> playback.playAndPause()
        }
    }
}