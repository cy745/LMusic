package com.lalilu.player

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import com.lalilu.common.Mathf
import com.lalilu.media.dao.MusicDao
import com.lalilu.media.toMediaMetaData
import java.util.*

class LMusicList(private val dataSource: MusicDao) {
    private var mNowMediaId: String? = null
    private var mNowPosition: Int = 0
    private val mOrderList = LinkedList<String>()

    fun next(): Uri? {
        mNowPosition = Mathf.clampInLoop(0, mOrderList.size - 1, mNowPosition + 1)
        mNowMediaId = mOrderList[mNowPosition]
        return getUriByMediaId(mNowMediaId)
    }

    fun previous(): Uri? {
        mNowPosition = Mathf.clampInLoop(0, mOrderList.size - 1, mNowPosition - 1)
        mNowMediaId = mOrderList[mNowPosition]
        return getUriByMediaId(mNowMediaId)
    }

    fun playByMediaId(mediaId: String?): Uri? {
        mediaId ?: return null
        mNowMediaId = mediaId
        mNowPosition = mOrderList.indexOf(mediaId)
        return getUriByMediaId(mNowMediaId)
    }

    fun setDataIn(mediaId: String) {
        if (!mOrderList.contains(mediaId)) {
            mOrderList.add(mediaId)
        }
    }

    fun setDataIn(mediaId: Collection<String>) {
        mediaId.forEach {
            if (!mOrderList.contains(it)) {
                mOrderList.add(it)
            }
        }
    }

    fun getNowMediaId(): String? {
        if (mNowMediaId == null) {
            mNowMediaId = mOrderList[mNowPosition]
        }
        return mNowMediaId!!
    }

    fun getNowUri(): Uri? {
        return getUriByMediaId(getNowMediaId())
    }

    fun getUriByMediaId(mediaId: String?): Uri? {
        return dataSource.getUriById(mediaId ?: return null)
    }

    fun getNowMetadata(): MediaMetadataCompat? {
        return getMetadataByMediaId(getNowMediaId())
    }

    fun getMetadataByMediaId(mediaId: String?): MediaMetadataCompat? {
        val media = dataSource.getMusicById(mediaId ?: return null) ?: return null
        return media.toMediaMetaData()
    }

    fun getOrderAndShowDataList(): List<String> {
        return mOrderList.map {
            val position =
                Mathf.clampInLoop(0, mOrderList.size - 1, mOrderList.indexOf(it), mNowPosition)
            mOrderList[position]
        }
    }

    fun removeMediaId(mediaId: String?) {
        if (mOrderList.contains(mediaId)) {
            mOrderList.remove(mediaId)
        }
    }

    fun updateOrderByNewList(newList: List<String>) {
        val first = mOrderList.indexOf(newList[0])
        val size = newList.size
        for (i: Int in 0 until size) {
            val position = Mathf.clampInLoop(0, size - 1, i, first)
            if (position >= mOrderList.size) {
                mOrderList.add(newList[i])
            } else {
                mOrderList[position] = newList[i]
            }
        }
    }
}