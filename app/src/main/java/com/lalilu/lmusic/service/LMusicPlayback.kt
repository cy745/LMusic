package com.lalilu.lmusic.service

import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.MutableLiveData
import com.lalilu.lmusic.manager.LMusicAudioFocusManager
import com.lalilu.lmusic.state.LMusicServiceViewModel
import com.lalilu.media.entity.Music
import com.lalilu.media.toMediaMetaData

class LMusicPlayback(
    mContext: Context,
    mState: LMusicServiceViewModel
) : BasePlayback<Music>(mContext) {
    override val nowPlaying: MutableLiveData<Music> = mState.nowPlayingMusic
    override val nowPlaylist: MutableLiveData<List<Music>> = mState.nowPlayingList

    fun setAudioFocusManager(mAudioFocusManager: LMusicAudioFocusManager): LMusicPlayback {
        this.mAudioFocusManager = mAudioFocusManager
        return this
    }

    fun setOnPlayerCallback(callback: Playback.OnPlayerCallback): LMusicPlayback {
        this.onPlayerCallback = callback
        return this
    }

    override fun getUriFromNowItem(nowPlaying: Music): Uri {
        return nowPlaying.musicUri
    }

    override fun getIdFromItem(item: Music): Long {
        return item.musicId
    }

    override fun getMetaDataFromItem(item: Music): MediaMetadataCompat {
        return item.toMediaMetaData()
    }

    override fun getItemById(list: List<Music>, mediaId: Long): Music? {
        val position = list.indexOf(Music(mediaId))
        if (position == -1) return null
        return list[position]
    }
}