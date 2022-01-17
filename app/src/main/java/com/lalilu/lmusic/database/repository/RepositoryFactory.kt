package com.lalilu.lmusic.database.repository

import android.content.Context
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.domain.entity.FullSongInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
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
        MutableStateFlow(sharedPref.getInt(Config.LAST_LIST_TYPE, LIST_TYPE_ALL))

    val list: Flow<List<FullSongInfo>> = _listType.flatMapLatest { type ->
        _listId.flatMapLatest ID@{ id ->
            if (type == LIST_TYPE_ALL) return@ID repository.songDao.getAllFullSongFlow()

            val ids = when (type) {
                LIST_TYPE_ALBUM -> repository.albumDao.getSongsIdByAlbumId(id)
                LIST_TYPE_PLAYLIST -> repository.albumDao.getSongsIdByAlbumId(id)
                else -> repository.playlistDao.getSongsIdByPlaylistId(id)
            }
            repository.songDao.getAllFullSongFlow().mapLatest { items ->
                ids.map { id -> items.first { it.song.songId == id } }
            }
        }
    }.flowOn(Dispatchers.IO)

    fun changeId(id: Long) = launch {
        _listId.emit(id)
    }
}

