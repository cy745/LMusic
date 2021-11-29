package com.lalilu.lmusic.domain.request

import com.lalilu.lmusic.database.LMusicDataBase
import com.lalilu.lmusic.domain.BaseRequest
import com.lalilu.lmusic.domain.entity.PlaylistWithSongs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AllPlayListRequest : BaseRequest<List<PlaylistWithSongs>>() {
    private val dao = LMusicDataBase.getInstance(null).playlistDao()

    override fun requireData() {
        GlobalScope.launch(Dispatchers.IO) {
            postData(dao.getAll())
        }
    }

    override fun requestData() {}
    override fun requestData(value: Any?) {}
}