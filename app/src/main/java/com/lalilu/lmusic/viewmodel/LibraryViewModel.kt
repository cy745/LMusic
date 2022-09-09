package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.datasource.MDataBase
import com.lalilu.lmusic.repository.LibraryDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryDataStore: LibraryDataStore,
    private val mDataBase: MDataBase
) : ViewModel() {

    /**
     * 请求获取每日推荐歌曲
     */
    fun requireDailyRecommends(): List<LSong> {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        libraryDataStore.apply {
            if (today == this.today.get()) {
                return dailyRecommends.get().mapNotNull {
                    Library.getSongOrNull(it)
                }
            }

            this.today.set(today)
            return Library.getSongs(num = 10, random = true).toList().also { list ->
                dailyRecommends.set(value = list.map { it.id })
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun requirePlayHistory(): Flow<List<LSong>> {
        return mDataBase.playHistoryDao().getFlow(20).mapLatest { list ->
            list.distinctBy { it.mediaId }.mapNotNull { Library.getSongOrNull(it.mediaId) }
        }
    }
}