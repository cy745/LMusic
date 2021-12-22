package com.lalilu.lmusic.domain.request

import com.lalilu.lmusic.database.dao.MPlaylistDao
import com.lalilu.lmusic.domain.BaseRequest
import com.lalilu.lmusic.domain.entity.PlaylistWithSongs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@Deprecated("使用Flow构建单向数据流，不需要Request了")
class AllPlayListRequest @Inject constructor(
    val dao: MPlaylistDao
) : BaseRequest<List<PlaylistWithSongs>>() {

    override fun requireData() {
        GlobalScope.launch(Dispatchers.IO) {
            postData(dao.getAll())
        }
    }

    override fun requestData() {}
    override fun requestData(value: Any?) {}
}