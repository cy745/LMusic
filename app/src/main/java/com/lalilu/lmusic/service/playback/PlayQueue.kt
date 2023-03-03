package com.lalilu.lmusic.service.playback

import android.net.Uri

interface PlayQueue<T> {
    fun getCurrent(): T?
    fun getPrevious(): T?
    fun getNext(): T?
    fun getById(id: String): T?
    fun getUriFromItem(item: T): Uri

    fun setCurrent(item: T)

    /**
     * 打乱队列
     */
    fun shuffle()

    /**
     * 恢复原顺序列表
     */
    fun recoverOrder()
}