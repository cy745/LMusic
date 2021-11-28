package com.lalilu.lmusic.service

import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.MutableLiveData
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.domain.entity.PlaylistWithSongs
import com.lalilu.lmusic.state.LMusicServiceViewModel

class MSongPlayback constructor(
    mContext: Context,
    mState: LMusicServiceViewModel
) : BasePlayback<MSong, PlaylistWithSongs>(mContext) {
    override val nowPlaylist: MutableLiveData<PlaylistWithSongs> = mState.playingPlaylist
    override val nowPlaying: MutableLiveData<MSong> = mState.playingSong

    override fun getUriFromNowItem(nowPlaying: MSong): Uri = nowPlaying.songUri
    override fun getIdFromItem(item: MSong): Long = item.songId
    override fun getMetaDataFromItem(item: MSong): MediaMetadataCompat =
        item.toMediaMetadataCompat()

    override fun getItemById(list: PlaylistWithSongs, mediaId: Long): MSong? =
        list.songs.find { it.songId == mediaId }

    override fun getSizeFromList(list: PlaylistWithSongs): Int = list.songs.size

    override fun getIndexOfFromList(list: PlaylistWithSongs, item: MSong): Int =
        list.songs.indexOfFirst { item.songId == it.songId }

    override fun getItemFromListByIndex(list: PlaylistWithSongs, index: Int): MSong {
        return list.songs[index]
    }
}