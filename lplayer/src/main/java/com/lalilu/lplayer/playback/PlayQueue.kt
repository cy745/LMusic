package com.lalilu.lplayer.playback

import android.net.Uri

interface PlayQueue<T> {
    fun getCurrent(): T?
    fun getPrevious(): T?
    fun getNext(): T?
    fun getShuffle(): T?
    fun getById(id: String): T?
    fun getUriFromItem(item: T): Uri

    fun updateQueue()
    fun setCurrent(item: T)
}