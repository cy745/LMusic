package com.lalilu.lmusic.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class CoverRepository(private val lMediaRepo: LMediaRepository) {
    fun fetch(id: Any?): Flow<Any?> {
        if (id == null || id !is String) return flowOf(id)

        return flowOf(lMediaRepo.requireSong(id) ?: id)
    }
}