package com.lalilu.lmusic.repository

import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmedia.repository.NetDataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class CoverRepository @Inject constructor(
    private val netDataRepo: NetDataRepository
) {
    fun fetch(id: Any?): Flow<Any?> {
        if (id == null || id !is String) return flowOf(id)

        return netDataRepo.getNetDataFlowById(id).mapLatest {
            it?.requireCoverUri() ?: Library.getSongOrNull(id) ?: id
        }
    }
}