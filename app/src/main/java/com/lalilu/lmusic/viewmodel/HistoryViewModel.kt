package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.component.extension.toState
import com.lalilu.lhistory.repository.HistoryRepository
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
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
        }.map { it.take(6) }
        .toState(emptyList(), viewModelScope)

    private val historyCountState = historyRepo
        .getHistoriesIdsMapWithCount()
        .toState(emptyMap(), viewModelScope)

    fun requiteHistoryCountById(mediaId: String): Int {
        return historyCountState.value[mediaId] ?: 0
    }

    fun clearHistories() {
        historyRepo.clearHistories()
    }
}