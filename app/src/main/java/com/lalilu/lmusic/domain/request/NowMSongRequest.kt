package com.lalilu.lmusic.domain.request

import com.lalilu.lmusic.database.LMusicDataBase
import com.lalilu.lmusic.domain.BaseRequest
import com.lalilu.lmusic.domain.entity.MSong

class NowMSongRequest : BaseRequest<MSong>() {
    private val dao = LMusicDataBase.getInstance(null).songDao()

    override fun requestData(value: Any?) {
        if (value == null || value !is Long) return

        setData(dao.getById(value))
    }
}