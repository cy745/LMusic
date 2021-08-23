package com.lalilu.lmusic.domain.request

import com.lalilu.lmusic.domain.BaseRequest
import com.lalilu.media.LMusicMediaModule
import com.lalilu.media.entity.Music

/**
 * 获取 playlist 的 request 对象，可通过 requestData 请求更新
 */
class NowPlaylistRequest : BaseRequest<List<Music>>() {
    val database = LMusicMediaModule.getInstance(null).database

    override fun requestData(value: Any?) {
        // 若传进来的 value 为空或非法，则重新从数据库中获取全部歌曲
        if (value == null || value !is Long) {
            requestData()
            return
        }
        // getPlaylistWithSongsByIdWithOrder 获取到的 playlist 保持存储时的顺序
        val playlist = database.playListDao().getPlaylistWithSongsByIdWithOrder(value)
        if (playlist != null) postData(playlist.musics) else requestData()
    }

    override fun requestData() {
        postData(database.musicDao().getAll())
    }
}