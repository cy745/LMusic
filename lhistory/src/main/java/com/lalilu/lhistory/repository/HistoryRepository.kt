package com.lalilu.lhistory.repository

import com.lalilu.lhistory.entity.LHistory
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    suspend fun preSaveHistory(history: LHistory): Long
    suspend fun updateHistory(id: Long, duration: Long, repeatCount: Int)
    fun clearHistories()

    fun getHistoriesFlow(limit: Int): Flow<List<LHistory>>
    fun getHistoriesWithCount(limit: Int): Flow<Map<LHistory, Int>>
    fun getHistoriesIdsMapWithCount(): Flow<Map<String, Int>>
    fun getHistoriesIdsMapWithLastTime(): Flow<Map<String, Long>>
    fun getHistoriesCountByMediaId(mediaId: String): Int
    fun getHistoriesLastTimeByMediaId(mediaId: String): Long
}