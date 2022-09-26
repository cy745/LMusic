package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.datasource.MDataBase
import com.lalilu.lmusic.repository.LibraryDataStore
import com.lalilu.lmusic.utils.toUpdatableFlow
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
     * 获取最近的播放记录
     *
     * 之前在 Composable 里用 CollectAsState 直接通过Flow获取了，
     * 但是那样每次Recompose的时候就会重新调用生成一个Flow，间接导致每次都会重新查询数据库
     */
    val lastPlayedStack = requirePlayHistory().asLiveData()
    val recentlyAdded = Library.getSongsFlow(15).asLiveData()
    val randomRecommends = Library.getSongsFlow(15, true).toUpdatableFlow()
    val randomRecommendsLiveData = randomRecommends.asLiveData()

    /**
     * 请求获取每日推荐歌曲
     */
    fun requireDailyRecommends(): List<LSong> {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        libraryDataStore.apply {
            if (today == this.today.get()) {
                dailyRecommends.get()
                    .takeIf { it.isNotEmpty() }
                    ?.mapNotNull { Library.getSongOrNull(it) }
                    ?.let { return it }
            }

            return Library.getSongs(num = 10, random = true).toList().also { list ->
                if (list.isNotEmpty()) {
                    this.today.set(today)
                    this.dailyRecommends.set(value = list.map { it.id })
                }
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