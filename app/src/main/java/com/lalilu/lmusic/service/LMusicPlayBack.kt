package com.lalilu.lmusic.service

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat

class LMusicPlayBack : MediaSessionCompat.Callback(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener {
    private var player: MediaPlayer? = null
    private var isPrepared: Boolean = false

    private fun checkPlayer() {
        player = player ?: MediaPlayer().apply {
            setOnPreparedListener(this@LMusicPlayBack)
            setOnCompletionListener(this@LMusicPlayBack)
        }
    }

    override fun onPlay() {
        checkPlayer()

        if (isPrepared) {
        }
    }

    override fun onPause() {

    }

    override fun onPrepare() {

    }

    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {

    }

    override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {

    }

    override fun onSkipToNext() {

    }

    override fun onSkipToPrevious() {

    }

    override fun onSeekTo(pos: Long) {

    }

    override fun onStop() {

    }

    override fun onCustomAction(action: String?, extras: Bundle?) {

    }

    override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
        return super.onMediaButtonEvent(mediaButtonEvent)
    }

    override fun onSetRepeatMode(repeatMode: Int) {

    }

    override fun onSetShuffleMode(shuffleMode: Int) {

    }

    override fun onPrepared(mp: MediaPlayer?) {
        isPrepared = true
    }

    override fun onCompletion(mp: MediaPlayer?) {
        TODO("Not yet implemented")
    }
}