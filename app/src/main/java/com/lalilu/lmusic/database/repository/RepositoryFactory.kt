package com.lalilu.lmusic.database.repository

import android.content.Context
import com.lalilu.lmusic.Config
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext


const val LIST_TYPE_ALL = 0
const val LIST_TYPE_ALBUM = 1
const val LIST_TYPE_PLAYLIST = 2

@Singleton
@ExperimentalCoroutinesApi
class RepositoryFactory @Inject constructor(
    @ApplicationContext context: Context,
    repository: ListRepository
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val sharedPref = context.getSharedPreferences(
        Config.SHARED_PLAYER, Context.MODE_PRIVATE
    )

    private val _listId =
        MutableStateFlow(sharedPref.getLong(Config.LAST_LIST_ID, 0L))
    private val _listType =
        MutableStateFlow(sharedPref.getInt(Config.LAST_LIST_TYPE, LIST_TYPE_PLAYLIST))

    val list = _listId.flatMapLatest { id ->
        _listType.flatMapLatest { type ->
            when (type) {
                // todo 需实现 静态获取歌曲列表，动态获取列表内歌曲信息
                LIST_TYPE_ALBUM -> repository.albumDao.getFullSongInfoListByIdFlow(id)
                else -> repository.playlistDao.getFullSongInfoListByIdFlow(id)
            }.mapNotNull { it }
        }
    }

    fun changeId(id: Long) = launch {
        _listId.emit(id)
    }
}

