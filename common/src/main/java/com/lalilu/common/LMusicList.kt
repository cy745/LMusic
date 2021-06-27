package com.lalilu.common

import androidx.recyclerview.widget.DiffUtil
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 *  针对使ForeGroundService和Activity双向数据同步而做的数据结构，
 *  由一个只保存Id的有序List，和一个Id对应Item的Map构成，
 *  mOrderList随意修改，mDataList不做删除，二者都可以避免重复数据，
 *  为的是需要使用到数据时无需从数据库中读取，
 *  数据统统从setValueIn输入
 *
 */
open class LMusicList<K, V> {
    var mOrderList = LinkedList<K>()
    val mDataList = LinkedHashMap<K, V>()
    var mNowItem: V? = null
    var mNowPosition: Int = 0
    fun size() = mOrderList.size

    /**
     * 获取正在播放的Item
     */
    fun getNowItem(): V? {
        return if (mNowItem == null) {
            mDataList[mOrderList[mNowPosition]]
        } else {
            mNowItem
        }
    }

    /**
     *  Item选中指针向前移动
     */
    fun last(): V? {
        mNowPosition = Mathf.clampInLoop(0, mOrderList.size - 1, mNowPosition - 1)
        mNowItem = mDataList[mOrderList[mNowPosition]]
        return mNowItem
    }

    /**
     *  Item选中指针向后移动
     */
    fun next(): V? {
        mNowPosition = Mathf.clampInLoop(0, mOrderList.size - 1, mNowPosition + 1)
        mNowItem = mDataList[mOrderList[mNowPosition]]
        return mNowItem
    }

    /**
     *  通过传入Key移动指针到指定Item位置进行播放（列表顺序不变）
     */
    open fun playByKey(key: K?): V? {
        key ?: return null
        mNowPosition = mOrderList.indexOf(key)
        mNowItem = mDataList[key]
        return mNowItem
    }

    open fun moveTo(key: K?): V? {
        return playByKey(key)
    }

    /**
     *  通过传入Key将选中Item移动至正在播放的位置（列表顺序有变）
     */
    open fun jumpTo(key: K?): V? {
        key ?: return null

        mOrderList.remove(key)
        mOrderList.add(mNowPosition, key)

        return playByKey(key)
    }

    /**
     *  传入key和value，可避免重复，不存在的key将直接添加至mOrderList的末尾
     */
    open fun setValueIn(key: K?, value: V?) {
        if (key == null || value == null) return
        mDataList[key] = value
        if (!mOrderList.contains(key)) mOrderList.add(key)
    }

    /**
     * 根据mOrderList的顺序获取mDataList的values
     */
    fun getOrderDataList(): List<V> {
        return mOrderList.map { mDataList[it] ?: return emptyList() }
    }

    /**
     * 根据mOrderList的顺序和当前正在播放的歌曲位置获取mDataList的values
     */
    fun getOrderAndShowDataList(): List<V> {
        return mOrderList.map {
            val position =
                Mathf.clampInLoop(0, mOrderList.size - 1, mOrderList.indexOf(it), mNowPosition)
            mDataList[mOrderList[position]] ?: return emptyList()
        }
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