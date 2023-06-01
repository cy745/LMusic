package com.lalilu.lplayer.extensions

import android.media.MediaPlayer
import android.os.CountDownTimer
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.LruCache
import androidx.annotation.IntRange

private var maxVolume: Float = 1f
private val timers = LruCache<Int, CountDownTimer>(3)
private val nowVolumes = LruCache<Int, Float>(3)

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    (1f - fraction) * start + fraction * stop

fun getNowVolume(id: Int): Float = minOf(nowVolumes[id] ?: 0f, maxVolume)

fun MediaPlayer.setMaxVolume(@IntRange(from = 0, to = 100) volume: Int) {
    maxVolume = (volume / 100f).coerceIn(0f, 1f)
    nowVolumes.put(audioSessionId, maxVolume)
    val temp = getNowVolume(audioSessionId)
    setVolume(temp, temp)
}

fun MediaPlayer.fadeStart(
    duration: Long = 500L,
    onFinished: () -> Unit = {}
) = synchronized(this) {
    timers[audioSessionId]?.cancel()
    val startValue = getNowVolume(audioSessionId)
    setVolume(startValue, startValue)

    // 当前未播放，则开始播放
    if (!isPlaying) start()

    val timer = object : CountDownTimer(duration, duration / 10L) {
        override fun onTick(millisUntilFinished: Long) {
            val fraction = 1f - millisUntilFinished * 1.0f / duration
            val volume = lerp(startValue, maxVolume, fraction)
            nowVolumes.put(audioSessionId, volume.coerceIn(0f, maxVolume))

            val temp = getNowVolume(audioSessionId)
            this@fadeStart.setVolume(temp, temp)
        }

        override fun onFinish() {
            setVolume(maxVolume, maxVolume)
            onFinished()
        }
    }
    timer.start()
    timers.put(audioSessionId, timer)
}

fun MediaPlayer.fadePause(
    duration: Long = 500L,
    onFinished: () -> Unit = {}
) = synchronized(this) {
    timers[audioSessionId]?.cancel()

    val startValue = getNowVolume(audioSessionId)

    val timer = object : CountDownTimer(duration, duration / 10L) {
        override fun onTick(millisUntilFinished: Long) {
            val fraction = 1f - millisUntilFinished * 1.0f / duration
            val volume = lerp(startValue, 0f, fraction)
            nowVolumes.put(audioSessionId, volume.coerceIn(0f, maxVolume))

            val temp = getNowVolume(audioSessionId)
            setVolume(temp, temp)
        }

        override fun onFinish() {
            setVolume(0f, 0f)
            if (isPlaying) pause()
            onFinished()
        }
    }
    timer.start()
    timers.put(audioSessionId, timer)
}

fun MediaSessionCompat.isPlaying(): Boolean {
    return PlaybackStateCompat.STATE_PLAYING == controller.playbackState?.state
}