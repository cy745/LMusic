package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lhistory.repository.HistoryRepository
import com.lalilu.component.extension.toState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val historyRepo: HistoryRepository
) : ViewModel() {
    val historyState = historyRepo
        .getHistoriesIdsMapWithLastTime()
        .flatMapLatest { map ->
            val ids = map.toList()
                .sortedByDescending { it.second }
                .map { it.first }
            LMedia.flowMapBy<LSong>(ids)
        }.toState(emptyList(), viewModelScope)

    private val historyCountState = historyRepo
        .getHistoriesIdsMapWithCount()
        .toState(emptyMap(), viewModelScope)

    fun requiteHistoryList(callback: (List<LSong>) -> Unit) {
        callback(historyState.value)
    }

    fun requiteHistoryCountById(mediaId: String): Int {
        return historyCountState.value[mediaId] ?: 0
    }

    fun clearHistories() {
        historyRepo.clearHistories()
    }
}