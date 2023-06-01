package com.lalilu.lplayer.notification

import android.app.Notification
import android.support.v4.media.session.MediaSessionCompat

interface Notifier {
    fun startForeground(mediaSession: MediaSessionCompat, callback: (Int, Notification) -> Unit)
    fun stopForeground(callback: () -> Unit)
    fun update(mediaSession: MediaSessionCompat)
    fun cancel()
}