package com.lalilu.lmusic

import androidx.recyclerview.widget.DiffUtil
import com.lalilu.lmusic.utils.Mathf
import java.util.*
import kotlin.collections.LinkedHashMap

open class LMusicList<K, V> {
    var mOrderList = LinkedList<K>()
    val mDataList = LinkedHashMap<K, V>()
    var mNowItem: V? = null
    var mNowPosition: Int = 0
    fun size() = mOrderList.size

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

    open fun playByKey(key: K?): V? {
        key ?: return null
        mNowPosition = mOrderList.indexOf(key)
        mNowItem = mDataList[key]
        return mNowItem
    }

    open fun jumpTo(key: K?): V? {
        key ?: return null

        mOrderList.remove(key)
        mOrderList.add(mNowPosition, key)

        mNowPosition = mOrderList.indexOf(key)
        mNowItem = mDataList[key]
        return mNowItem
    }

    open fun moveTo(key: K?): V? {
        key ?: return null
        mNowPosition = mOrderList.indexOf(key)
        mNowItem = mDataList[key]
        return mNowItem
    }

    open fun setValueIn(key: K?, value: V?) {
        if (key == null || value == null) return
        mDataList[key] = value
        if (!mOrderList.contains(key)) mOrderList.add(key)
    }

    fun getOrderDataList(): List<V> {
        return mOrderList.map { mDataList[it] ?: return emptyList() }
    }

    companion object {
        const val LIST_TRANSFORM_ACTION = "list_transform_action"
        const val ACTION_JUMP_TO = 0
        const val ACTION_MOVE_TO = 1

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