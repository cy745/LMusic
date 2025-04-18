package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.component.extension.toState
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datastore.TempSp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import java.util.Calendar

@Single
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    private val tempSp: TempSp
) : ViewModel() {
    val recentlyAdded = LMedia.getFlow<LSong>()
        .mapLatest { it.take(15) }
        .toState(emptyList(), viewModelScope)

    val dailyRecommends = tempSp.dailyRecommends.flow(true)
        .flatMapLatest { LMedia.flowMapBy<LSong>(it ?: emptyList()) }
        .toState(emptyList(), viewModelScope)

    init {
        checkOrUpdateToday()
    }

    fun checkOrUpdateToday() = viewModelScope.launch {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

        if (today != tempSp.dayOfYear.value || dailyRecommends.value.isEmpty()) {
            val ids = LMedia.get<LSong>().shuffled().take(10).map { it.id }
            if (ids.isEmpty()) return@launch

            tempSp.dayOfYear.value = today
            tempSp.dailyRecommends.value = ids
        }
    }

    fun forceUpdate() = viewModelScope.launch {
        val ids = LMedia.get<LSong>().shuffled().take(10).map { it.id }
        if (ids.isEmpty()) return@launch

        tempSp.dailyRecommends.value = ids
    }
}