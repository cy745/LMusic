package com.lalilu.lmusic.repository

import com.lalilu.common.toCachedFlow
import com.lalilu.lmusic.datastore.HistoryDao
import com.lalilu.lmusic.datastore.LHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

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

    override fun saveHistory(vararg history: LHistory) {
        launch { historyDao.save(history.toList()) }
    }

    override fun saveHistories(history: List<LHistory>) {
        launch { historyDao.save(history) }
    }

    override fun preSaveHistory(history: LHistory) {
        launch { historyDao.save(history.copy(duration = -1L)) }
    }

    override fun updatePreSavedHistory(contentId: String, duration: Long) {
        launch { historyDao.updatePreSavedHistory(contentId, duration) }
    }

    override fun removePreSavedHistory(contentId: String) {
        launch { historyDao.deletePreSavedHistory(contentId) }
    }

    override fun clearHistories() {
        launch { historyDao.clear() }
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