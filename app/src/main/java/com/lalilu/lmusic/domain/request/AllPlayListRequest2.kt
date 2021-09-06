package com.lalilu.lmusic.domain.request

import com.lalilu.lmusic.LMusicPlaylistMMKV
import com.lalilu.lmusic.domain.BaseRequest
import com.lalilu.media.entity.LPlaylist

class AllPlayListRequest2 : BaseRequest<ArrayList<LPlaylist>>() {
    private val lMusicPlaylistMMKV = LMusicPlaylistMMKV.getInstance()

    override fun requestData() {
        postData(lMusicPlaylistMMKV.readAllPlaylist().playlists)
    }
}