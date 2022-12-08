package com.lalilu.lmusic.service.playback

import android.net.Uri

interface PlayQueue<T> {
    fun getCurrent(): T?
    fun getPrevious(random: Boolean): T?
    fun getNext(random: Boolean): T?
    fun getById(id: String): T?
    fun getUriFromItem(item: T): Uri

    fun setCurrent(item: T)
}