package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.component.extension.toState
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datastore.TempKV
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import java.util.Calendar

@Single
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel : ViewModel() {
    val recentlyAdded = LMedia.getFlow<LSong>()
        .mapLatest { it.take(15) }
        .toState(emptyList(), viewModelScope)

    val dailyRecommends = TempKV.dailyRecommends.flow()
        .flatMapLatest { LMedia.flowMapBy<LSong>(it) }
        .toState(emptyList(), viewModelScope)

    init {
        checkOrUpdateToday()
    }

    fun checkOrUpdateToday() = viewModelScope.launch {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

        if (today != TempKV.dayOfYear.value || dailyRecommends.value.isEmpty()) {
            val ids = LMedia.get<LSong>().shuffled().take(10).map { it.id }
            if (ids.isEmpty()) return@launch

            TempKV.dayOfYear.value = today
            TempKV.dailyRecommends.value = ids
        }
    }

    fun forceUpdate() = viewModelScope.launch {
        val ids = LMedia.get<LSong>().shuffled().take(10).map { it.id }
        if (ids.isEmpty()) return@launch

        TempKV.dailyRecommends.value = ids
    }
}