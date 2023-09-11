package com.lalilu.lplayer.playback

import android.net.Uri

interface PlayQueue<T> {
    fun getCurrent(): T?
    fun getPrevious(): T?
    fun getNext(): T?
    fun getShuffle(): T?
    fun getById(id: String): T?
    fun getUriFromItem(item: T): Uri
    fun moveToPrevious(item: T)
    fun moveToNext(item: T)
    fun setCurrent(item: T)
}