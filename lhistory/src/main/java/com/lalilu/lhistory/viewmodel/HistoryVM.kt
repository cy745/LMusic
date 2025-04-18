package com.lalilu.lhistory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.lalilu.component.extension.toState
import com.lalilu.lhistory.repository.HistoryRepository
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@OptIn(ExperimentalCoroutinesApi::class)
@Single
class HistoryVM(
    val historyRepo: HistoryRepository
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

    val pager = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = true,
        ),
        pagingSourceFactory = {
            historyRepo.getAllData()
        }
    ).flow.cachedIn(viewModelScope)

    fun getHistoryPlayedIds(block: (list: List<String>) -> Unit) = viewModelScope.launch {
        val list = historyRepo.getHistoriesIdsMapWithLastTime()
            .firstOrNull()
            ?.toList()
            ?.sortedByDescending { it.second }
            ?.map { it.first }
            ?: emptyList()
        block(list)
    }
}