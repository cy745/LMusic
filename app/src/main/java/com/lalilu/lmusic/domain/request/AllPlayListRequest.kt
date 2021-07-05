package com.lalilu.lmusic.domain.request

import com.lalilu.lmusic.domain.BaseRequest
import com.lalilu.media.LMusicMediaModule
import com.lalilu.media.entity.PlaylistWithMusics

class AllPlayListRequest : BaseRequest<List<PlaylistWithMusics>>() {
    override fun requestData() {
        val database = LMusicMediaModule.getInstance(null).database
        val list = database.playListDao().getPlaylistWithSongs()
        postData(list)
    }
}