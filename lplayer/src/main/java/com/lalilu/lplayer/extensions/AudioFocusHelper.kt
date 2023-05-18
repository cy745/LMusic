package com.lalilu.lplayer.extensions

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

@Suppress("DEPRECATION")
class AudioFocusHelper(context: Context) : AudioManager.OnAudioFocusChangeListener {
    companion object {
        var ignoreAudioFocus: Boolean = false
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var resumeOnGain: Boolean = false
    private var focusRequest: AudioFocusRequest? = null
    var onPlay: () -> Unit = {}
    var onPause: () -> Unit = {}
    var isPlaying: () -> Boolean = { false }


    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (resumeOnGain) {
                    if (!ignoreAudioFocus) {
                        onPlay()
                    }
                }
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                resumeOnGain = false
                if (!ignoreAudioFocus) {
                    onPause()
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                resumeOnGain = isPlaying()
                if (!ignoreAudioFocus) {
                    onPause()
                }
            }
        }
    }

    fun abandon() {
        resumeOnGain = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            audioManager.abandonAudioFocus(this)
        }
    }

    fun request(): Int {
        resumeOnGain = false
        if (ignoreAudioFocus) return AudioManager.AUDIOFOCUS_REQUEST_GRANTED

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
            audioManager.requestAudioFocus(focusRequest!!)
        } else {
            audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }
}