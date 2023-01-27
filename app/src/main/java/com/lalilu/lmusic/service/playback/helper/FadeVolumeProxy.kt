package com.lalilu.lmusic.service.playback.helper

import android.media.MediaPlayer
import android.os.CountDownTimer
import androidx.annotation.IntRange

class FadeVolumeProxy(
    private val mPlayer: MediaPlayer,
    private var mDuration: Long = 500L
) {
    companion object {
        var maxVolume = 1f
            private set

        fun setMaxVolume(@IntRange(from = 0, to = 100) volume: Int) {
            maxVolume = (volume / 100f).coerceIn(0f, 1f)
        }
    }

    private var countDownTimer: CountDownTimer? = null
    private var nowVolume = 0f
        get() = minOf(field, maxVolume)

    fun setMaxVolume(@IntRange(from = 0, to = 100) volume: Int) {
        Companion.setMaxVolume(volume)
        mPlayer.setVolume(maxVolume, maxVolume)
    }

    fun fadeStart(
        duration: Long = mDuration,
        onFinished: () -> Unit = {}
    ) {
        countDownTimer?.cancel()

        mPlayer.setVolume(nowVolume, nowVolume)
        mPlayer.start()
        val startValue = nowVolume
        countDownTimer = object : CountDownTimer(duration, duration / 10L) {
            override fun onTick(millisUntilFinished: Long) {
                val fraction = 1f - millisUntilFinished * 1.0f / duration
                val volume = lerp(start = startValue, stop = maxVolume, fraction = fraction)
                    .coerceIn(0f, 1f)
                nowVolume = volume
                mPlayer.setVolume(volume, volume)
            }

            override fun onFinish() {
                mPlayer.setVolume(maxVolume, maxVolume)
                onFinished()
            }
        }
        countDownTimer?.start()
    }

    fun fadePause(
        duration: Long = mDuration,
        onFinished: () -> Unit = {}
    ) {
        countDownTimer?.cancel()

        val startValue = nowVolume
        countDownTimer = object : CountDownTimer(duration, duration / 10L) {
            override fun onTick(millisUntilFinished: Long) {
                val fraction = 1f - millisUntilFinished * 1.0f / duration
                val volume = lerp(start = startValue, stop = 0f, fraction = fraction)
                    .coerceIn(0f, maxVolume)
                nowVolume = volume
                mPlayer.setVolume(volume, volume)
            }

            override fun onFinish() {
                mPlayer.setVolume(0f, 0f)
                mPlayer.pause()
                onFinished()
            }
        }
        countDownTimer?.start()
    }

    private fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return (1f - fraction) * start + fraction * stop
    }
}