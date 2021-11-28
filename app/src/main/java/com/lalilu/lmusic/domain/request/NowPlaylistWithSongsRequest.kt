package com.lalilu.lmusic.domain.request

import com.lalilu.lmusic.database.LMusicDataBase
import com.lalilu.lmusic.domain.BaseRequest
import com.lalilu.lmusic.domain.entity.PlaylistWithSongs

/**
 * 获取 PlaylistWithSongs 的 request 对象，可通过 requestData 请求更新
 */
class NowPlaylistWithSongsRequest : BaseRequest<PlaylistWithSongs>() {
    private val dao = LMusicDataBase.getInstance(null).playlistDao()

    override fun requestData(value: Any?) {
        if (value == null || value !is Long) return

        setData(dao.getById(value))
    }
}