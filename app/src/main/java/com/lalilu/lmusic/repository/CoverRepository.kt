package com.lalilu.lmusic.repository

import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class CoverRepository {
    fun fetch(id: Any?): Flow<Any?> {
        if (id == null || id !is String) return flowOf(id)

        return flowOf(LMedia.get<LSong>(id) ?: id)
    }
}