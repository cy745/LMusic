package com.lalilu.lplayer.notification

import android.app.Notification
import android.support.v4.media.session.MediaSessionCompat

interface Notifier {
    fun bindMediaSession(mediaSession: MediaSessionCompat)
    fun startForeground(callback: (Int, Notification) -> Unit)
    fun stopForeground(callback: () -> Unit)
    fun update()
    fun cancel()
}