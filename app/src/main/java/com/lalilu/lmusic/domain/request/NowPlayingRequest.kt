package com.lalilu.lmusic.domain.request

import com.lalilu.lmusic.LMusicPlaylistMMKV
import com.lalilu.lmusic.domain.BaseRequest
import com.lalilu.lmusic.domain.entity.LSong

class NowPlayingRequest : BaseRequest<LSong>() {
    val lMusicPlaylistMMKV = LMusicPlaylistMMKV.getInstance()

    override fun requestData(value: Any?) {
        if (value == null || value !is Long) {
            requestData()
            return
        }

        val song = lMusicPlaylistMMKV.readLocalSongById(value)
        if (song != null) setData(song) else requestData()
    }

    override fun requestData() {
    }
}