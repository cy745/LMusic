package com.lalilu.lmusic.domain.request

import com.lalilu.lmusic.database.LMusicDataBase
import com.lalilu.lmusic.domain.BaseRequest
import com.lalilu.lmusic.domain.entity.MSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 获取 MSong 的 request 对象，可通过 requireData 请求更新
 */
class NowMSongRequest : BaseRequest<MSong>() {
    private val dao = LMusicDataBase.getInstance(null).songDao()

    override fun requireData(value: Any?) {
        super.requireData(value)
        if (value == null || value !is Long) return

        GlobalScope.launch(Dispatchers.IO) {
            postData(dao.getById(value))
        }
    }

    override fun requestData() {}
    override fun requestData(value: Any?) {}
}