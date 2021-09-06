package com.lalilu.lmusic.domain.request

import com.lalilu.lmusic.LMusicPlaylistMMKV
import com.lalilu.lmusic.domain.BaseRequest
import com.lalilu.media.entity.LPlaylist

/**
 * 获取 playlist 的 request 对象，可通过 requestData 请求更新
 */
class NowPlaylistRequest2 : BaseRequest<LPlaylist>() {
    private val lMusicPlaylistMMKV = LMusicPlaylistMMKV.getInstance()

    override fun requestData(value: Any?) {
        // 若传进来的 value 为空或非法，则重新从数据库中获取全部歌曲
        if (value == null || value !is Long) {
            requestData()
            return
        }
        val playlist = lMusicPlaylistMMKV.readPlaylistById(value)
        if (playlist != null) postData(playlist) else requestData()
    }

    override fun requestData() {
        postData(lMusicPlaylistMMKV.readLocalPlaylist())
    }
}