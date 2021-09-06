package com.lalilu.lmusic.domain.request

import com.lalilu.lmusic.LMusicPlaylistMMKV
import com.lalilu.lmusic.domain.BaseRequest
import com.lalilu.lmusic.domain.entity.LPlaylist

class AllPlayListRequest : BaseRequest<ArrayList<LPlaylist>>() {
    private val lMusicPlaylistMMKV = LMusicPlaylistMMKV.getInstance()

    override fun requestData() {
        postData(lMusicPlaylistMMKV.readAllPlaylist().playlists)
    }
}