package com.lalilu.lmusic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.lalilu.lmusic.service2.MusicService

class LMusicAudioManager constructor(private val service: MusicService) :
    AudioManager.OnAudioFocusChangeListener {

    var nowVolume = 0f

    private var volumeAnimator: ValueAnimator? = null

    fun fadeStart() {
        if (volumeAnimator != null) volumeAnimator?.cancel()
        volumeAnimator = ValueAnimator.ofFloat(nowVolume, 1f).also {
            it.duration = (nowVolume / 1 * 400).toLong()
            it.addUpdateListener { v ->
                val value = v.animatedValue as Float
                nowVolume = value
                service.musicPlayer.setVolume(value, value)
            }
            it.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    if (!service.musicPlayer.isPlaying) service.musicPlayer.start()
                }
            })
        }
        volumeAnimator?.start()
    }

    fun fadePause() {
        if (volumeAnimator != null) volumeAnimator?.cancel()
        volumeAnimator = ValueAnimator.ofFloat(nowVolume, 0f).also {
            it.duration = (nowVolume / 1 * 400).toLong()
            it.addUpdateListener { v ->
                val value = v.animatedValue as Float
                nowVolume = value
                service.musicPlayer.setVolume(value, value)
            }
            it.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    if (service.musicPlayer.isPlaying) service.musicPlayer.pause()
                }
            })
        }
        volumeAnimator?.start()
    }

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

    fun getAudioFocus(): Int {
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
        println("[onAudioFocusChange]: $focusChange")
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            this.service.musicSessionCallback.onPause()
        }
    }
}