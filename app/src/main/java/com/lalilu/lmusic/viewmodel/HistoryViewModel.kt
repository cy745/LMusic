package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.repository.HistoryRepository
import com.lalilu.lmusic.utils.extension.toState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    historyRepo: HistoryRepository,
) : ViewModel() {
    val historyPreviewState = historyRepo
        .getHistoriesFlow(5)
        .mapLatest { list ->
            list.mapNotNull { LMedia.getSongOrNull(it.contentId) }
        }
        .toState(emptyList(), viewModelScope)

    val historyState = historyRepo
        .getHistoriesWithCount(1000)
        .mapLatest { map ->
            map.mapNotNull { entry ->
                LMedia.getSongOrNull(entry.key.contentId)
                    ?.let { it to entry.value }
            }
        }
        .toState(emptyList(), viewModelScope)

    fun requiteHistoryList(callback: (List<LSong>) -> Unit) {
        callback(historyState.value.map { it.first })
    }
}