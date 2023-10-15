package com.lalilu.lplayer.extensions

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.LruCache
import androidx.annotation.IntRange
import com.blankj.utilcode.util.LogUtils
import java.net.URLDecoder

object PlayerVolumeHelper {
    private var maxVolume: Float = 1f
    private val timers = LruCache<Int, CountDownTimer>(5)
    private val nowVolumes = LruCache<Int, Float>(5)

    fun getMaxVolume(): Float = maxVolume
    fun getNowVolume(id: Int): Float {
        val volume = nowVolumes.get(id) ?: maxVolume.also { nowVolumes.put(id, it) }
        return minOf(volume, maxVolume)
    }

    fun updateMaxVolume(maxVolume: Float) {
        this.maxVolume = maxVolume
    }

    fun updateNowVolume(id: Int, volume: Float) {
        nowVolumes.put(id, volume)
    }

    fun cancelTimer(id: Int) {
        timers[id]?.cancel()
    }

    fun addTimer(id: Int, timer: CountDownTimer) {
        timers.put(id, timer)
    }
}

fun MediaPlayer.setMaxVolume(@IntRange(from = 0, to = 100) volume: Int) {
    val maxVolume = (volume / 100f).coerceIn(0f, 1f)
    val sessionId = audioSessionId

    PlayerVolumeHelper.updateMaxVolume(maxVolume)
    PlayerVolumeHelper.updateNowVolume(sessionId, maxVolume)
    val temp = PlayerVolumeHelper.getNowVolume(sessionId)
    setVolume(temp, temp)
}

fun MediaPlayer.fadeStart(
    duration: Long = 500L,
    onFinished: () -> Unit = {},
) = synchronized(this) {
    val sessionId = audioSessionId
    PlayerVolumeHelper.cancelTimer(sessionId)

    val startValue = PlayerVolumeHelper.getNowVolume(sessionId)
    setVolume(startValue, startValue)

    // 当前未播放，则开始播放
    if (!isPlaying) start()

    val timer = object : CountDownTimer(duration, duration / 10L) {
        override fun onTick(millisUntilFinished: Long) {
            val maxVolume = PlayerVolumeHelper.getMaxVolume()
            val fraction = 1f - (millisUntilFinished * 1.0f / duration)
            val volume = lerp(startValue, maxVolume, fraction).coerceIn(0f, maxVolume)

            PlayerVolumeHelper.updateNowVolume(sessionId, volume)
            val temp = PlayerVolumeHelper.getNowVolume(sessionId)

            runCatching { setVolume(temp, temp) }.getOrElse { cancel() }
        }

        override fun onFinish() {
            val maxVolume = PlayerVolumeHelper.getMaxVolume()
            PlayerVolumeHelper.updateNowVolume(sessionId, maxVolume)
            runCatching { setVolume(maxVolume, maxVolume) }.getOrElse { cancel() }
            onFinished()
        }
    }
    timer.start()
    PlayerVolumeHelper.addTimer(sessionId, timer)
}

fun MediaPlayer.fadePause(
    duration: Long = 500L,
    onFinished: () -> Unit = {},
) = synchronized(this) {
    val sessionId = audioSessionId
    PlayerVolumeHelper.cancelTimer(sessionId)
    val startValue = PlayerVolumeHelper.getNowVolume(sessionId)

    val timer = object : CountDownTimer(duration, duration / 10L) {
        override fun onTick(millisUntilFinished: Long) {
            val maxVolume = PlayerVolumeHelper.getMaxVolume()
            val fraction = 1f - (millisUntilFinished * 1.0f / duration)
            val volume = lerp(startValue, 0f, fraction).coerceIn(0f, maxVolume)

            PlayerVolumeHelper.updateNowVolume(sessionId, volume)
            val temp = PlayerVolumeHelper.getNowVolume(sessionId)

            runCatching { setVolume(temp, temp) }.getOrElse { cancel() }
        }

        override fun onFinish() {
            PlayerVolumeHelper.updateNowVolume(sessionId, 0f)
            runCatching { setVolume(0f, 0f) }.getOrElse { cancel() }
            if (isPlaying) pause()
            onFinished()
        }
    }
    timer.start()
    PlayerVolumeHelper.addTimer(sessionId, timer)
}

fun MediaPlayer.loadSource(context: Context, uri: Uri, handleNetUrl: (String) -> String = { it }) {
    if (uri.scheme == "content" || uri.scheme == "file") {
        setDataSource(context, uri)
    } else {
        // url 的长度可能会超长导致异常
        val url = URLDecoder.decode(uri.toString(), "UTF-8")
        val proxyUrl = handleNetUrl(url)

        if (url != proxyUrl) {
            LogUtils.i("MediaPlayer: cacheProxy", "url: $url, proxyUrl: $proxyUrl")
        }
        setDataSource(proxyUrl)
    }
}

fun MediaSessionCompat.isPlaying(): Boolean {
    return PlaybackStateCompat.STATE_PLAYING == controller.playbackState?.state
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    (1f - fraction) * start + fraction * stop

fun <T> List<T>.getNextOf(item: T, cycle: Boolean = false): T? {
    val nextIndex = indexOf(item) + 1
    return getOrNull(if (cycle) nextIndex % size else nextIndex)
}

fun <T> List<T>.getPreviousOf(item: T, cycle: Boolean = false): T? {
    var previousIndex = indexOf(item) - 1
    if (previousIndex < 0 && cycle) {
        previousIndex = size - 1
    }
    return getOrNull(previousIndex)
}

fun <T : Any> List<T>.move(from: Int, to: Int): List<T> = toMutableList().apply {
    val targetIndex = if (from < to) to else to + 1
    val temp = removeAt(from)
    add(targetIndex, temp)
}

fun <T : Any> List<T>.add(index: Int = -1, item: T): List<T> = toMutableList().apply {
    if (index == -1) add(item) else add(index, item)
}

fun <T : Any> List<T>.removeAt(index: Int): List<T> = toMutableList().apply {
    removeAt(index)
}