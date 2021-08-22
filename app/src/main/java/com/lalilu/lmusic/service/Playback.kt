package com.lalilu.lmusic.service

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat

/**
 * 描述播放器可操作行为的基类
 */
interface Playback {

    interface OnPlayerCallback {
        fun onPlaybackStateChanged(newState: Int)
        fun onMetadataChanged(metadata: MediaMetadataCompat?)
    }

    /**
     * 正常播放
     */
    fun play()

    /**
     * 正常暂停
     */
    fun pause()

    /**
     * 正常播放暂停切换
     */
    fun playAndPause()

    /**
     * 当播放器为null时需执行的操作
     */
    fun rebuild()

    /**
     * 传入Uri进行播放
     */
    fun playByUri(uri: Uri)

    /**
     * 传入MediaId进行播放
     */
    fun playByMediaId(mediaId: Long?)

    /**
     * 播放下一首
     */
    fun next()

    /**
     * 播放上一首
     */
    fun previous()

    /**
     * 停止播放，释放播放器资源
     */
    fun stop()

    /**
     * 传入进度跳转到指定位置
     */
    fun seekTo(position: Number)

    /**
     * 获取当前播放进度
     */
    fun getPosition(): Long

    fun onCompletion()

    fun onPlaybackStateChanged(state: Int)

    fun onMetadataChanged()
}