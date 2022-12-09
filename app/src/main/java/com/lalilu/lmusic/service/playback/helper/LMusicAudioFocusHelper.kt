package com.lalilu.lmusic.service.playback.helper

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.lalilu.lmusic.datastore.SettingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LMusicAudioFocusHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore
) : AudioManager.OnAudioFocusChangeListener {
    private val am: AudioManager
        get() = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var focusRequest: AudioFocusRequest? = null
    private var resumeOnGain: Boolean = false
    var onPlay: () -> Unit = {}
    var onPause: () -> Unit = {}

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (resumeOnGain) {
                    settingsDataStore.apply {
                        if (ignoreAudioFocus.get() != true) {
                            onPlay()
                        }
                    }
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                resumeOnGain = false
                settingsDataStore.apply {
                    if (ignoreAudioFocus.get() != true) {
                        onPause()
                    }
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                resumeOnGain = true
                settingsDataStore.apply {
                    if (ignoreAudioFocus.get() != true) {
                        onPause()
                    }
                }
            }
        }
    }

    fun abandonAudioFocus() {
        resumeOnGain = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { am.abandonAudioFocusRequest(it) }
        } else {
            am.abandonAudioFocus(this)
        }
    }

    fun requestAudioFocus(): Int {
        resumeOnGain = false
        val enable = settingsDataStore.run { ignoreAudioFocus.get() != true }
        if (!enable) return AudioManager.AUDIOFOCUS_REQUEST_GRANTED

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build()
            am.requestAudioFocus(focusRequest!!)
        } else {
            am.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }
}