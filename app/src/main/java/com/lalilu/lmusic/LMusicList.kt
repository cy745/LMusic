package com.lalilu.lmusic

import androidx.recyclerview.widget.DiffUtil
import com.lalilu.lmusic.utils.Mathf
import java.util.*
import kotlin.collections.LinkedHashMap

class LMusicList<K, V> {
    private var mOrderList = LinkedList<K>()
    private val mDataList = LinkedHashMap<K, V>()
    private var mNowItem: V? = null
    private var mNowPosition: Int = 0

    fun size() = mOrderList.size
    fun getOrderList(): LinkedList<K> = mOrderList

    fun getNowItem(): V? {
        return mNowItem ?: mDataList[mOrderList[mNowPosition]]
    }

    fun last(): V? {
        mNowPosition = Mathf.clamp(0, mOrderList.size - 1, mNowPosition - 1)
        mNowItem = mDataList[mOrderList[mNowPosition]]
        return mNowItem
    }

    fun next(): V? {
        mNowPosition = Mathf.clamp(0, mOrderList.size - 1, mNowPosition + 1)
        mNowItem = mDataList[mOrderList[mNowPosition]]
        return mNowItem
    }

    fun getShowingListWithOffsetPosition(position: Int): V? {
        return mDataList[mOrderList[mNowPosition + position]]
    }

    fun jumpTo(key: K?): V? {
        key ?: return null
        mOrderList.remove(key)
        mOrderList.add(mNowPosition, key)
        mNowPosition = mOrderList.indexOf(key)
        mNowItem = mDataList[key]
        return mNowItem
    }

    fun moveTo(key: K?): V? {
        key ?: return null
        mNowPosition = mOrderList.indexOf(key)
        mNowItem = mDataList[key]
        return mNowItem
    }

    fun setValueIn(key: K, value: V) {
        mDataList[key] = value
        if (!mOrderList.contains(key)) mOrderList.add(key)
    }

    fun getSelectedByPosition(position: Int): V? {
        return mDataList[mOrderList[position]]
    }

    fun getSelectedByKey(key: K?): V? {
        key ?: return null
        return mDataList[key]
    }

    fun getOrderDataList(): List<V> {
        return mOrderList.map { mDataList[it] ?: return emptyList() }
    }

    fun swapSelectedToTop(key: K?) {
        Companion.swapSelectedToTop(mOrderList, key)
    }

    fun swapSelectedToBottom(key: K?) {
        Companion.swapSelectedToBottom(mOrderList, key)
    }

    fun getNextByKey(key: K): V? {
        var next = mOrderList.indexOf(key) + 1
        if (next >= mOrderList.size) next = 0
        val nextKey = mOrderList[next]
        return mDataList[nextKey]
    }

    fun getPreviousByKey(key: K): V? {
        var previous = mOrderList.indexOf(key) - 1
        if (previous < 0) previous = mOrderList.size - 1
        val previousKey = mOrderList[previous]
        return mDataList[previousKey]
    }

    fun getDiffCallBack(newList: List<K>): LMusicListDiffCallback<K> {
        return LMusicListDiffCallback(mOrderList, newList)
    }

    fun setNewOrderList(newList: LinkedList<K>) {
        mOrderList = newList
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