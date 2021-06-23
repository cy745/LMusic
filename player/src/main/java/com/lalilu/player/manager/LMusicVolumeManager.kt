package com.lalilu.player.manager

import android.media.MediaPlayer
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

class LMusicVolumeManager(private val mPlayer: MediaPlayer) {
    private var nowVolume = 0f
    private var mSpringAnimation: SpringAnimation? = null

    private fun reCreateSpringAnimation() {
        mSpringAnimation =
            SpringAnimation(mPlayer, volumePropertyCompat, 1f).apply {
                this.spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                this.spring.stiffness = SpringForce.STIFFNESS_VERY_LOW
                this.addEndListener { _, canceled, value, _ ->
                    if (!canceled && value == 0f) mPlayer.pause()
                }
            }
    }

    fun fadeStart() {
        if (mSpringAnimation == null) reCreateSpringAnimation()
        mSpringAnimation?.cancel()
        if (!mPlayer.isPlaying) mPlayer.start()
        mSpringAnimation?.animateToFinalPosition(1f)
    }

    fun fadePause() {
        if (mSpringAnimation == null) reCreateSpringAnimation()
        mSpringAnimation?.cancel()
        mSpringAnimation?.animateToFinalPosition(0f)
    }

    private val volumePropertyCompat =
        object : FloatPropertyCompat<MediaPlayer>("volume") {
            override fun getValue(`object`: MediaPlayer?): Float {
                return nowVolume
            }

            override fun setValue(`object`: MediaPlayer?, value: Float) {
                `object`?.setVolume(value, value)
                nowVolume = value
            }
        }
}