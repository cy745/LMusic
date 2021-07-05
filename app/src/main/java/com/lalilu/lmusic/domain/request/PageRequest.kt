package com.lalilu.lmusic.domain.request

import com.lalilu.lmusic.domain.BaseRequest

class PageRequest : BaseRequest<Int>() {

    override fun requestData() {
        data.value = 1
    }
}