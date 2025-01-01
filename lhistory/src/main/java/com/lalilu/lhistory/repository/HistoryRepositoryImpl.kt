package com.lalilu.lhistory.repository

import androidx.paging.PagingSource
import com.lalilu.common.toCachedFlow
import com.lalilu.lhistory.entity.LHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import kotlin.coroutines.CoroutineContext

@Single(binds = [HistoryRepository::class])
class HistoryRepositoryImpl(
    private val historyDao: HistoryDao
) : HistoryRepository, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val countMap = historyDao
        .getFlowIdsMapWithCount(Int.MAX_VALUE)
        .distinctUntilChanged()
        .toCachedFlow()
        .also { it.launchIn(this) }

    private val lastTimeMap = historyDao
        .getFlowIdsMapWithLastTime(Int.MAX_VALUE)
        .distinctUntilChanged()
        .toCachedFlow()
        .also { it.launchIn(this) }

    override suspend fun preSaveHistory(history: LHistory): Long = withContext(Dispatchers.IO) {
        historyDao.save(history.copy(duration = -1L))
    }

    override suspend fun updateHistory(id: Long, duration: Long, repeatCount: Int) {
        historyDao.updateHistory(id = id, duration = duration, repeatCount = repeatCount)
    }

    override fun clearHistories() {
        launch { historyDao.clear() }
    }

    override fun getAllData(): PagingSource<Int, LHistory> {
        return historyDao.getAllData()
    }

    override fun getHistoriesFlow(limit: Int): Flow<List<LHistory>> {
        return historyDao
            .getFlow(limit)
            .distinctUntilChanged()
    }

    override fun getHistoriesWithCount(limit: Int): Flow<Map<LHistory, Int>> {
        return historyDao
            .getFlowWithCount(limit)
            .distinctUntilChanged()
    }

    override fun getHistoriesCountByMediaId(mediaId: String): Int {
        return countMap.get()?.get(mediaId) ?: 0
    }

    override fun getHistoriesLastTimeByMediaId(mediaId: String): Long {
        return lastTimeMap.get()?.get(mediaId) ?: 0L
    }

    override fun getHistoriesIdsMapWithCount(): Flow<Map<String, Int>> {
        return countMap
    }

    override fun getHistoriesIdsMapWithLastTime(): Flow<Map<String, Long>> {
        return lastTimeMap
    }
}