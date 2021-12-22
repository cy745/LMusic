package com.lalilu.lmusic.domain.request

import com.lalilu.lmusic.database.dao.MSongDao
import com.lalilu.lmusic.domain.BaseRequest
import com.lalilu.lmusic.domain.entity.MSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 获取 MSong 的 request 对象，可通过 requireData 请求更新
 */
@Deprecated("使用Flow构建单向数据流，不需要Request了")
class NowMSongRequest @Inject constructor(
    val dao: MSongDao
) : BaseRequest<MSong>() {

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