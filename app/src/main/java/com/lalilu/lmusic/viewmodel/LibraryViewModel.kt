package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datastore.TempSp
import com.lalilu.lmusic.utils.extension.toState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    private val tempSp: TempSp
) : ViewModel() {
    val recentlyAdded = LMedia.getFlow<LSong>().mapLatest { it.take(15) }
        .toState(emptyList(), viewModelScope)

    val dailyRecommends = tempSp.dailyRecommends.flow(true)
        .flatMapLatest { LMedia.flowMapBy<LSong>(it ?: emptyList()) }
        .toState(emptyList(), viewModelScope)

    fun checkOrUpdateToday() {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

        if (today != tempSp.dayOfYear.get() || dailyRecommends.value.isEmpty()) {
            val ids = LMedia.get<LSong>().shuffled().take(10).map { it.id }
            if (ids.isEmpty()) return

            tempSp.dayOfYear.set(today)
            tempSp.dailyRecommends.set(ids)
        }
    }
}