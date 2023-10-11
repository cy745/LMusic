package com.lalilu.lplayer.playback

import android.net.Uri
import com.lalilu.lplayer.extensions.add
import com.lalilu.lplayer.extensions.getNextOf
import com.lalilu.lplayer.extensions.getPreviousOf
import com.lalilu.lplayer.extensions.move
import com.lalilu.lplayer.extensions.removeAt

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

abstract class IdBaseQueue<T> : UpdatableQueue<T> {
    abstract fun getIdFromItem(item: T): String

    open var playingId: String? = null
        protected set
    open var items: List<String> = emptyList()
        protected set

    override fun setIds(ids: List<String>) {
        items = ids
    }

    override fun setCurrentId(id: String?) {
        playingId = id
    }

    override fun getCurrent(): T? =
        playingId
            ?.let { getById(it) }

    override fun getPrevious(): T? =
        playingId
            ?.let { items.getPreviousOf(it) }
            ?.let { getById(it) }

    override fun getNext(): T? =
        playingId
            ?.let { items.getNextOf(it) }
            ?.let { getById(it) }

    override fun moveToNext(item: T) {
        val newItems = items.toMutableList()
        val playingId = playingId
        val itemId = getIdFromItem(item)

        val targetItemIndex = newItems.indexOf(itemId)
        if (targetItemIndex != -1) {
            newItems.removeAt(targetItemIndex)
        }

        val playingIndex = playingId
            ?.let { newItems.indexOf(it) }
            ?.takeIf { it >= 0 }
            ?: 0

        // TODO 待验证
        if (playingIndex + 1 >= newItems.size) {
            newItems.add(itemId)
        } else {
            newItems.add(playingIndex + 1, itemId)
        }
        items = newItems
    }

    override fun moveToPrevious(item: T) {
        val newItems = items.toMutableList()
        val playingId = playingId
        val itemId = getIdFromItem(item)

        val targetItemIndex = newItems.indexOf(itemId)
        if (targetItemIndex != -1) {
            newItems.removeAt(targetItemIndex)
        }

        val playingIndex = playingId
            ?.let { newItems.indexOf(it) }
            ?.takeIf { it >= 0 }
            ?: 0

        newItems.add(playingIndex, itemId)
        items = newItems
    }

    override fun getShuffle(): T? {
        val items = items
        val playingId = playingId
        val playingIndex = items.indexOf(playingId)
        val endIndex = playingIndex + 20
        var targetIndex: Int? = null
        var retryCount = 5

        if (items.size <= 20 * 2) {
            while (true) {
                targetIndex = items.indices.randomOrNull() ?: break
                if (targetIndex != playingIndex || retryCount-- <= 0) break
            }
        } else {
            var targetRange = items.indices - playingIndex.rangeTo(endIndex)

            if (endIndex >= items.size) {
                targetRange = targetRange - 0.rangeTo(endIndex - items.size)
            }

            targetIndex = targetRange.randomOrNull()
        }

        targetIndex ?: return null
        return getById(items[targetIndex])
    }


    override fun moveByIndex(from: Int, to: Int) {
        items = items.move(from, to)
    }

    override fun addToIndex(index: Int, id: String) {
        items = items.add(index, id)
    }

    override fun removeById(id: String) {
        items = items.removeAt(items.indexOf(id))
    }

    override fun getNextIdOf(id: String): String? = items.getNextOf(id)
    override fun getPreviousIdOf(id: String): String? = items.getPreviousOf(id)
}