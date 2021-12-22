package com.lalilu.lmusic.domain.request

import com.lalilu.lmusic.database.dao.MPlaylistDao
import com.lalilu.lmusic.domain.BaseRequest
import com.lalilu.lmusic.domain.entity.PlaylistWithSongs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 获取 PlaylistWithSongs 的 request 对象，可通过 requireData 请求更新
 */
@Deprecated("使用Flow构建单向数据流，不需要Request了")
class NowPlaylistWithSongsRequest @Inject constructor(
    val dao: MPlaylistDao
) : BaseRequest<PlaylistWithSongs>() {

    override fun requireData(value: Any?) {
        super.requestData(value)
        if (value == null || value !is Long) return

        GlobalScope.launch(Dispatchers.IO) {
            postData(dao.getById(value))
        }
    }

    override fun requestData() {}
    override fun requestData(value: Any?) {}
}