package com.lalilu.lplayer.playback

import android.net.Uri
import com.lalilu.lplayer.extensions.add
import com.lalilu.lplayer.extensions.move
import com.lalilu.lplayer.extensions.removeAt

interface PlayQueue<T> {
    fun isListLooping(): Boolean
    fun getById(id: String): T?
    fun getUriFromItem(item: T): Uri

    fun getIds(): List<String>
    fun getCurrentId(): String?
    fun getSize(): Int = getIds().size
    fun indexOf(id: String): Int = getIds().indexOf(id)
    fun getOrNull(index: Int): String? = getIds().getOrNull(index)

    fun getCurrentIndex(): Int = getCurrentId()
        ?.let { indexOf(it) } ?: -1

    fun getCurrent(): T? = getOrNull(getCurrentIndex())
        ?.let { getById(it) }

    fun getNextIndex(): Int = (getCurrentIndex() + 1)
        .let { if (isListLooping()) it % getSize() else it }

    fun getPreviousIndex(): Int = (getCurrentIndex() - 1)
        .let { if (isListLooping()) it % getSize() else it }

    fun getNextId(): String? = getOrNull(getNextIndex())
    fun getPreviousId(): String? = getOrNull(getPreviousIndex())
    fun getNext(): T? = getNextId()?.let { getById(it) }
    fun getPrevious(): T? = getPreviousId()?.let { getById(it) }

    fun getShuffle(): T?
}

interface UpdatableQueue<T> : PlayQueue<T> {
    fun setIds(ids: List<String>)
    fun setCurrentId(id: String?)

    fun addToNext(id: String): Boolean {
        val itemIndex = indexOf(id)
        // 该元素已存在于列表中，则返回添加失败
        if (itemIndex != -1) return false

        val nextIndex = getNextIndex()
        //  该元素已经处于下一个位置，则返回移动失败
        if (itemIndex == nextIndex) return false

        setIds(getIds().add(nextIndex, id))
        return true
    }

    fun addToPrevious(id: String): Boolean {
        val itemIndex = indexOf(id)
        // 该元素已存在于列表中，则返回添加失败
        if (itemIndex != -1) return false

        val previousIndex = getPreviousIndex()
        //  该元素已经处于上一个位置，则返回移动失败
        if (itemIndex == previousIndex) return false

        setIds(getIds().add(previousIndex, id))
        return true
    }

    fun moveToNext(id: String): Boolean {
        val itemIndex = indexOf(id)
        // 该元素不存在与列表中，则返回移动失败
        if (itemIndex == -1) return false

        val nextIndex = getNextIndex()
        //  该元素已经处于下一个位置，则返回移动失败
        if (itemIndex == nextIndex) return false

        setIds(getIds().move(itemIndex, nextIndex))
        return true
    }

    fun moveToPrevious(id: String): Boolean {
        val itemIndex = indexOf(id)
        // 该元素不存在与列表中，则返回移动失败
        if (itemIndex == -1) return false

        val previousIndex = getPreviousIndex()
        //  该元素已经处于上一个位置，则返回移动失败
        if (itemIndex == previousIndex) return false

        setIds(getIds().move(itemIndex, previousIndex))
        return true
    }

    fun remove(id: String) = setIds(getIds().removeAt(indexOf(id)))
    fun moveByIndex(from: Int, to: Int) = setIds(getIds().move(from, to))
}

abstract class BaseQueue<T> : UpdatableQueue<T> {
    override fun getShuffle(): T? {
        val items = getIds()
        val playingId = getCurrentId()
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
}