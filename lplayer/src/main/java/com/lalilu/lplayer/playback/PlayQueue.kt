package com.lalilu.lplayer.playback

import android.net.Uri

interface PlayQueue<T> {
    fun getCurrent(): T?
    fun getNext(): T?
    fun getPrevious(): T?
    fun getNextIdOf(id: String): String?
    fun getPreviousIdOf(id: String): String?
    fun getShuffle(): T?
    fun getById(id: String): T?
    fun getUriFromItem(item: T): Uri
}

interface UpdatableQueue<T> : PlayQueue<T> {
    fun setIds(ids: List<String>)
    fun setCurrentId(id: String?)
    fun moveToPrevious(item: T)
    fun moveToNext(item: T)
    fun moveByIndex(from: Int, to: Int)
    fun removeById(id: String)
    fun addToIndex(index: Int, id: String)
}