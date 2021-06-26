package com.lalilu.player.manager

import android.app.Service
import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

class LMusicAudioFocusManager constructor(
    private val service: Service
) : AudioManager.OnAudioFocusChangeListener {
    var onAudioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null

    fun abandonAudioFocus() {
        val am = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
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
        val am = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
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
        onAudioFocusChangeListener?.let {
            onAudioFocusChangeListener!!.onAudioFocusChange(focusChange)
        }
    }
}