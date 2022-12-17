package com.lalilu.lmusic.service.pusher

import android.app.Service
import android.support.v4.media.session.MediaSessionCompat

interface Pusher {

    var getService: () -> Service?
    var getMediaSession: () -> MediaSessionCompat?

    fun update()
    fun cancel()

    fun destroy()
}