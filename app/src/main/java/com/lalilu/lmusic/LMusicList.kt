package com.lalilu.lmusic

import androidx.recyclerview.widget.DiffUtil
import java.util.*
import kotlin.collections.LinkedHashMap

class LMusicList<K, V> {
    private var mList = LinkedList<K>()
    private val mDataList = LinkedHashMap<K, V>()

    fun size() = mList.size

    fun setValueIn(key: K, value: V) {
        mDataList[key] = value
        if (!mList.contains(key)) mList.add(key)
    }

    fun getSelectedByPosition(position: Int): V? {
        return mDataList[mList[position]]
    }

    fun getSelectedByKey(key: K?): V? {
        key ?: return null
        return mDataList[key]
    }

    fun getOrderList(): LinkedList<K> {
        return mList
    }

    fun getOrderDataList(): List<V> {
        return mList.map { mDataList[it] ?: return emptyList() }
    }

    fun swapSelectedToTop(key: K?) {
        Companion.swapSelectedToTop(mList, key)
    }

    fun swapSelectedToBottom(key: K?) {
        Companion.swapSelectedToBottom(mList, key)
    }

    fun getNextByKey(key: K): V? {
        var next = mList.indexOf(key) + 1
        if (next >= mList.size) next = 0
        val nextKey = mList[next]
        return mDataList[nextKey]
    }

    fun getPreviousByKey(key: K): V? {
        var previous = mList.indexOf(key) - 1
        if (previous < 0) previous = mList.size - 1
        val previousKey = mList[previous]
        return mDataList[previousKey]
    }

    fun getDiffCallBack(newList: List<K>): LMusicListDiffCallback<K> {
        return LMusicListDiffCallback(mList, newList)
    }

    fun setNewOrderList(newList: LinkedList<K>) {
        mList = newList
    }

    companion object {
        fun <K> swapSelectedToTop(list: LinkedList<K>, key: K?) {
            key ?: return
            list.remove(key)
            list.add(0, key)
        }

        fun <K> swapSelectedToBottom(list: LinkedList<K>, key: K?) {
            key ?: return
            list.remove(key)
            list.add(key)
        }

        class LMusicListDiffCallback<K> constructor(
            private val oldList: List<K>,
            private val newList: List<K>
        ) : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.size
            override fun getNewListSize(): Int = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }
        }
    }
}