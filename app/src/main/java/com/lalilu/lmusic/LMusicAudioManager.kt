package com.lalilu.lmusic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.view.animation.AccelerateInterpolator
import com.lalilu.lmusic.service2.MusicService

class LMusicAudioManager constructor(private val service: MusicService) :
    AudioManager.OnAudioFocusChangeListener {

    fun fadeStart() = volumeAnimator.start()
    fun fadePause() = volumeAnimator.reverse()

    private var volumeAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f).also {
        it.addUpdateListener { v ->
            val value = v.animatedValue as Float
            service.musicPlayer.setVolume(value, value)
        }
        it.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                if (!isReverse) service.musicPlayer.start()
            }

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                if (isReverse) service.musicPlayer.pause()
            }
        })
        it.duration = 400
        it.interpolator = AccelerateInterpolator()
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