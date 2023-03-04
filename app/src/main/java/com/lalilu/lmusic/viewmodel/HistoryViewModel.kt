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
    historyRepo: HistoryRepository,
    lMediaRepo: LMediaRepository
) : ViewModel() {
    val historyPreviewState = historyRepo
        .getHistoriesFlow(5)
        .mapLatest { list ->
            list.mapNotNull { lMediaRepo.requireSong(it.contentId) }
        }
        .toState(emptyList(), viewModelScope)

    val historyState = historyRepo
        .getHistoriesWithCount(1000)
        .mapLatest { map ->
            map.mapNotNull { entry ->
                lMediaRepo.requireSong(entry.key.contentId)
                    ?.let { it to entry.value }
            }
        }
        .toState(emptyList(), viewModelScope)

    fun requiteHistoryList(callback: (List<LSong>) -> Unit) {
        callback(historyState.value.map { it.first })
    }
}