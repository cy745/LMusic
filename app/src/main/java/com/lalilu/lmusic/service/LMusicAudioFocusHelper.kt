package com.lalilu.lmusic.service

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

class LMusicAudioFocusHelper(
    private val context: Context,
    private var enable: Boolean = true,
    private val callback: (Int) -> Unit
) : AudioManager.OnAudioFocusChangeListener {

    fun abandonAudioFocus() {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            am.abandonAudioFocusRequest(
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(this)
                    .build()
            )
        } else {
            am.abandonAudioFocus(this)
        }
    }

    fun requestAudioFocus(): Int {
        if (!enable) return AudioManager.AUDIOFOCUS_REQUEST_GRANTED

        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            am.requestAudioFocus(
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(this)
                    .build()
            )
        } else {
            am.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (enable) {
            callback(focusChange)
        }
    }
}