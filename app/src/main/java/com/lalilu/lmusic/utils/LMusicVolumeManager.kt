package com.lalilu.lmusic.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.media.MediaPlayer

class LMusicVolumeManager(private val mPlayer: MediaPlayer) {
    private var nowVolume = 0f
    private var volumeAnimator: ValueAnimator? = null

    fun fadeStart() {
        if (volumeAnimator != null) volumeAnimator?.cancel()
        volumeAnimator = ValueAnimator.ofFloat(nowVolume, 1f).also {
            it.duration = (nowVolume / 1 * 400).toLong()
            it.addUpdateListener { v ->
                val value = v.animatedValue as Float
                nowVolume = value
                mPlayer.setVolume(value, value)
            }
            it.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    if (!mPlayer.isPlaying) mPlayer.start()
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
                mPlayer.setVolume(value, value)
            }
            it.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    if (mPlayer.isPlaying) mPlayer.pause()
                }
            })
        }
        volumeAnimator?.start()
    }
}