package com.lalilu.lmusic.domain.request

import com.lalilu.lmusic.domain.BaseRequest
import com.lalilu.media.LMusicMediaModule
import com.lalilu.media.entity.Music

class NowPlayingRequest : BaseRequest<Music>() {
    val database = LMusicMediaModule.getInstance(null).database

    override fun requestData(value: Any?) {
        println(value)
        if (value == null || value !is Long) {
            requestData()
            return
        }
        val music = database.musicDao().getMusicById(value)
        if (music != null) postData(music) else requestData()
    }

    override fun requestData() {
    }
}