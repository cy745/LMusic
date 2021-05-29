package com.lalilu.lmusic.service2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat

class MusicSessionCallback : MediaSessionCompat.Callback() {

    override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
        return super.onMediaButtonEvent(mediaButtonEvent)
    }

    override fun onPrepare() {
        super.onPrepare()
    }

    override fun onPlay() {
        super.onPlay()
    }

    override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
        super.onPlayFromUri(uri, extras)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onSeekTo(pos: Long) {
        super.onSeekTo(pos)
    }
}