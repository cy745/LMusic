package com.lalilu.lmusic.service.playback

import android.net.Uri

interface Player {
    var listener: Listener?
    var isPrepared: Boolean
    var isPlaying: Boolean
    var isStopped: Boolean

    /**
     * 加载歌曲文件
     */
    fun load(uri: Uri, startWhenReady: Boolean)
    fun play()
    fun pause()
    fun stop()
    fun seekTo(durationMs: Number)

    /**
     * 请求音频焦点
     */
    fun requestAudioFocus(): Boolean
    fun getPosition(): Long

    interface Listener {
        fun onLStart()
        fun onLStop()
        fun onLPlay()
        fun onLPause()
        fun onLSeekTo(newDurationMs: Number)
        fun onLPrepared()
        fun onLCompletion()
    }
}
