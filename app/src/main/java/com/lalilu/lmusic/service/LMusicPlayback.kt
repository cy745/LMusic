package com.lalilu.lmusic.service

import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.MutableLiveData
import com.lalilu.lmusic.domain.entity.LPlaylist
import com.lalilu.lmusic.domain.entity.LSong
import com.lalilu.lmusic.state.LMusicServiceViewModel
import com.lalilu.lmusic.utils.toMediaMetaData

@Deprecated("MSongPlayback 替代，后期删除")
class LMusicPlayback(
    mContext: Context,
    mState: LMusicServiceViewModel
) : BasePlayback<LSong, LPlaylist>(mContext) {
    override val nowPlaying: MutableLiveData<LSong> = mState.nowPlayingMusic
    override val nowPlaylist: MutableLiveData<LPlaylist> = mState.nowPlayingList

    override fun getUriFromNowItem(nowPlaying: LSong): Uri {
//        return Uri.parse(nowPlaying.mLocalInfo?.mData)
        return nowPlaying.mLocalInfo?.mUri!!
    }

    override fun getIdFromItem(item: LSong): Long {
        return item.mId
    }

    override fun getMetaDataFromItem(item: LSong): MediaMetadataCompat {
        return item.toMediaMetaData()
    }

    override fun getItemById(list: LPlaylist, mediaId: Long): LSong? {
        val position = list.songs?.indexOf(LSong(mediaId, ""))
        if (position == -1 || position == null) return null
        return list.songs?.get(position)
    }

    override fun getSizeFromList(list: LPlaylist): Int {
        return list.songs?.size ?: 0
    }

    override fun getIndexOfFromList(list: LPlaylist, item: LSong): Int {
        return list.songs?.indexOf(item) ?: -1
    }

    override fun getItemFromListByIndex(list: LPlaylist, index: Int): LSong {
        return list.songs?.get(index)!!
    }
}