package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.repository.HistoryRepository
import com.lalilu.lmusic.repository.LMediaRepository
import com.lalilu.lmusic.utils.extension.toState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val historyRepo: HistoryRepository,
    lMediaRepo: LMediaRepository
) : ViewModel() {
    val historyState = historyRepo
        .getHistoriesIdsMapWithLastTime()
        .mapLatest { map ->
            map.mapNotNull { entry ->
                val item = lMediaRepo.requireSong(entry.key) ?: return@mapNotNull null
                item to entry.value
            }.sortedByDescending { it.second }
                .map { it.first }
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