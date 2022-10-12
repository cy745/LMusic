package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.database.sort
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.datasource.entity.SongInPlaylist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ItemPosition
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlaylistsViewModel @Inject constructor() : ViewModel() {
    val playlists = mutableStateOf(emptyList<LPlaylist>())

    private val playlistsLiveData = Library.getPlaylistWithDetailFlow()
        .mapLatest { it.sort(true) }
        .onEach { playlists.value = it }
        .launchIn(viewModelScope)

    fun movePlaylist(from: ItemPosition, to: ItemPosition) {
        playlists.value = playlists.value.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

//    suspend fun getSongsByPlaylistId(playlistId: Long): List<MediaItem> =
//        withContext(Dispatchers.IO) {
//            return@withContext dataBase.songInPlaylistDao()
//                .getAllByPlaylistId(playlistId)
//                .mapNotNull {
//                    mediaSource.getItemById(ITEM_PREFIX + it.mediaId)
//                }
//        }

    fun swapTwoPlaylists(from: LPlaylist, to: LPlaylist) = viewModelScope.launch(Dispatchers.IO) {
        Library.swapTwoPlaylists(from, to)
    }

    fun createNewPlaylist(title: String) = viewModelScope.launch(Dispatchers.IO) {
        Library.createPlaylist(LPlaylist(_title = title))
    }

    fun removePlaylists(playlist: List<LPlaylist>) = viewModelScope.launch(Dispatchers.IO) {
        Library.removePlaylists(playlist)
    }

    fun copyCurrentPlayingPlaylist() = viewModelScope.launch(Dispatchers.IO) {
//        val playlistTitle = "复制歌单: (${HistoryManager.currentPlayingIds.size})"
//        val playlist = MPlaylist(playlistTitle = playlistTitle)
//        dataBase.playlistDao().save(playlist)
//        dataBase.songInPlaylistDao().save(
//            HistoryManager.currentPlayingIds.map {
//                SongInPlaylist(
//                    playlistId = playlist.playlistId,
//                    mediaId = it
//                )
//            }
//        )
    }

    fun addSongsIntoPlaylists(mediaIds: List<String>, playlistIds: List<Long>) =
        viewModelScope.launch(Dispatchers.IO) {
            val songInPlaylist = playlistIds.flatMap { playlistId ->
                mediaIds.map { mediaId ->
                    SongInPlaylist(playlistId = playlistId, mediaId = mediaId)
                }
            }
        }
}