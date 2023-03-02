package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.*
import com.lalilu.lmedia.repository.HistoryRepository
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.repository.LibraryRepository
import com.lalilu.lmusic.utils.extension.toState
import com.lalilu.lmusic.utils.extension.toUpdatableFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import java.util.*

class LibraryViewModel constructor(
    private val lMusicSp: LMusicSp,
    private val libraryRepo: LibraryRepository,
) : ViewModel() {
    val recentlyAdded = libraryRepo.getSongsFlow(15).toState(emptyList(), viewModelScope)

    /**
     * 请求获取每日推荐歌曲
     */
    fun requireDailyRecommends(): List<LSong> {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        var dayOfYear by lMusicSp.dayOfYear
        var dailyRecommends by lMusicSp.dailyRecommends
        if (dayOfYear == today) {
            dailyRecommends
                .mapNotNull(LMedia::getSongOrNull)
                .takeIf(List<LSong>::isNotEmpty)
                ?.let { return it }
        }

        return libraryRepo.getSongs(10, true).also { list ->
            if (list.isNotEmpty()) {
                dayOfYear = today
                dailyRecommends = list.map { it.id }
            }
        }
    }
}