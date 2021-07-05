package com.lalilu.lmusic.domain.request

import com.lalilu.lmusic.domain.BaseRequest
import com.lalilu.media.LMusicMediaModule
import com.lalilu.media.entity.Music

class NowPlaylistRequest : BaseRequest<List<Music>>() {
    val database = LMusicMediaModule.getInstance(null).database

    override fun requestData(value: Any?) {
        if (value == null || value !is Long) {
            requestData()
            return
        }
        val playlist = database.playListDao().getPlaylistWithSongsByIdWithOrder(value)
        if (playlist != null) postData(playlist.musics) else requestData()
    }

    override fun requestData() {
        postData(database.musicDao().getAll())
    }
}