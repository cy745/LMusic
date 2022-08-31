package com.lalilu.lmusic.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.media.MediaPlayer
import androidx.dynamicanimation.animation.FloatPropertyCompat

class FadeVolumeProxy(
    private val player: MediaPlayer
) {
    private var nowVolume = 0f
    private var valueAnimator: ValueAnimator? = null

    fun fadeStart() {
        if (valueAnimator == null) reCreateAnimation()
        valueAnimator?.cancel()
        if (!player.isPlaying) player.start()
        valueAnimator?.start()
    }

    fun fadePause() {
        if (valueAnimator == null) reCreateAnimation()
        valueAnimator?.cancel()
        valueAnimator?.reverse()
    }

    private fun reCreateAnimation() {
        valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            var isReversed = false
            var startValue = 0f

            this.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator, isReverse: Boolean) {
                    startValue = volumePropertyCompat.getValue(player)
                    isReversed = isReverse
                }

                override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                    if (isReverse && animatedValue as Float == 0f) {
                        player.pause()
                    }
                }
            })
            this.addUpdateListener {
                var value = it.animatedValue as Float

                value =
                    if (isReversed) startValue * value else startValue + (1 - startValue) * value
                volumePropertyCompat.setValue(player, value)
            }
        }
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