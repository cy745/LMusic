package com.lalilu.lmusic.ui.seekbar

import android.support.v4.media.session.PlaybackStateCompat
import android.view.MotionEvent

interface AbstractSeekBar {
    fun updateProgress(event: MotionEvent, moving: Boolean)

    fun updatePosition(playbackStateCompat: PlaybackStateCompat)
    fun updatePosition(position: Long)

    fun onTouchUp()
    fun onTouchUpWithChange()

    fun onTouchMove()

    fun onTouchDown()
}