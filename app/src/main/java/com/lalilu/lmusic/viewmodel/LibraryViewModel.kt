package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.*
import com.lalilu.lmusic.datastore.TempSp
import com.lalilu.lmusic.repository.LMediaRepository
import com.lalilu.lmusic.utils.extension.toState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel constructor(
    private val tempSp: TempSp,
    private val lMediaRepo: LMediaRepository,
) : ViewModel() {
    val recentlyAdded = lMediaRepo.getSongsFlow(15).toState(emptyList(), viewModelScope)

    private val today = MutableStateFlow(Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
    val dailyRecommends = lMediaRepo.songsFlow.flatMapLatest { songs ->
        today.mapLatest { day ->
            return@mapLatest if (day == tempSp.dayOfYear.get()) {
                tempSp.dailyRecommends.get().mapNotNull { id ->
                    songs.firstOrNull { it.id == id }
                }
            } else {
                tempSp.dayOfYear.set(Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
                return@mapLatest songs.shuffled().take(10).also {
                    tempSp.dailyRecommends.set(it.map(LSong::id))
                }
            }
        }
    }.toState(emptyList(), viewModelScope)

    fun checkOrUpdateToday() {
        today.tryEmit(Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
    }
}