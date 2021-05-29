package com.lalilu.lmusic.service2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat

class MusicSessionCallback constructor(private val musicPlayer: MusicPlayer) :
    MediaSessionCompat.Callback() {

    override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
        return super.onMediaButtonEvent(mediaButtonEvent)
    }

    override fun onPrepare() {
        musicPlayer.prepare()
    }

    override fun onPlay() {
        musicPlayer.play()
    }

    override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
        musicPlayer.setDataSource(uri ?: return)
    }

    override fun onPause() {
        musicPlayer.pause()
    }

    override fun onStop() {
        musicPlayer.stop()
    }

    override fun onSeekTo(pos: Long) {
        musicPlayer.seekTo(pos)
    }
}