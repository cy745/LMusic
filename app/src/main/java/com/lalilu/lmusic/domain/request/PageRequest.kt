package com.lalilu.lmusic.domain.request

import com.lalilu.lmusic.domain.BaseRequest

@Deprecated("用处不大，准备删除")
class PageRequest : BaseRequest<Int>() {

    override fun requestData() {
        data.value = 1
    }
}