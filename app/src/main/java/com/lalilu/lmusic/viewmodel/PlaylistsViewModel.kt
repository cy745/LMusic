package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.database.sort
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.datasource.entity.SongInPlaylist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlaylistsViewModel @Inject constructor() : ViewModel() {
    val playlistsLiveData =
        Library.getPlaylistWithDetailFlow()
            .mapLatest { it.sort(true) }
            .asLiveData(viewModelScope.coroutineContext)

//    suspend fun getSongsByPlaylistId(playlistId: Long): List<MediaItem> =
//        withContext(Dispatchers.IO) {
//            return@withContext dataBase.songInPlaylistDao()
//                .getAllByPlaylistId(playlistId)
//                .mapNotNull {
//                    mediaSource.getItemById(ITEM_PREFIX + it.mediaId)
//                }
//        }

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