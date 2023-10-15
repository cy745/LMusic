package com.lalilu.lplayer.playback

import android.net.Uri

interface Player {
    var listener: Listener?
    var isPrepared: Boolean
    var isPlaying: Boolean
    var isStopped: Boolean
    var couldPlayNow: () -> Boolean
    var handleNetUrl: (String) -> String

    fun play()
    fun pause()
    fun stop()
    fun seekTo(durationMs: Number)
    fun destroy()

    /**
     * 加载歌曲文件
     */
    fun load(uri: Uri, startWhenReady: Boolean)
    fun preloadNext(uri: Uri)
    fun confirmPreloadNext()        // 确认当前歌曲播放完成，可以播放下一首
    fun resetPreloadNext()          // 重置预加载的下一首歌曲

    fun getPosition(): Long
    fun getDuration(): Long
    fun getBufferedPosition(): Long

    fun getVolume(): Int
    fun getMaxVolume(): Int
    fun setMaxVolume(volume: Int)

    fun interface Listener {
        fun onPlayerEvent(event: PlayerEvent)
    }
}

sealed class PlayerEvent {
    data object OnPlay : PlayerEvent()              // 开始加载
    data object OnStart : PlayerEvent()             // 开始播放
    data object OnPause : PlayerEvent()             // 暂停播放
    data object OnStop : PlayerEvent()              // 停止播放
    data object OnPrepared : PlayerEvent()          // 加载完成
    data object OnNextPrepared : PlayerEvent()      // 预加载完成

    data class OnCompletion(val nextItemReady: Boolean) : PlayerEvent()
    data class OnCreated(val playerId: Any) : PlayerEvent()
    data class OnError(val throwable: Exception) : PlayerEvent()
    data class OnSeekTo(val newDurationMs: Number) : PlayerEvent()
}
