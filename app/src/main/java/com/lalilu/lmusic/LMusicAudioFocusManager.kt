package com.lalilu.lmusic

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.lalilu.lmusic.service2.MusicService

class LMusicAudioFocusManager constructor(
    private val service: MusicService,
    private val listener: AudioManager.OnAudioFocusChangeListener
) {
    fun abandonAudioFocus() {
        val am = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            am.abandonAudioFocusRequest(
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(listener)
                    .build()
            )
        } else {
            am.abandonAudioFocus(listener)
        }
    }

    fun getAudioFocus(): Int {
        val am = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            am.requestAudioFocus(
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(listener)
                    .build()
            )
        } else {
            am.requestAudioFocus(
                listener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }
}