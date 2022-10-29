package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.database.sort
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.indexer.Library
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ItemPosition
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlaylistsViewModel @Inject constructor() : ViewModel() {
    var playlists by mutableStateOf(emptyList<LPlaylist>())

    fun onMovePlaylist(from: ItemPosition, to: ItemPosition) {
        val toIndex = playlists.indexOfFirst { it._id == to.key }
        val fromIndex = playlists.indexOfFirst { it._id == from.key }

        if (toIndex < 0 || fromIndex < 0) return
        playlists = playlists.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
    }

    fun canDragOver(pos: ItemPosition) = playlists.any { pos.key == it._id }


    init {
        Library.playlistRepoFlow
            .flatMapLatest { it?.getAllPlaylistWithDetailFlow() ?: flowOf(emptyList()) }
            .mapLatest { it.sort(true) }
            .onEach { playlists = it }
            .launchIn(viewModelScope)
    }

    fun createNewPlaylist(title: String) =
        viewModelScope.launch(Dispatchers.IO) {
            Library.playlistRepo?.savePlaylist(LPlaylist(_title = title))
        }

    fun removePlaylists(playlist: List<LPlaylist>) =
        viewModelScope.launch(Dispatchers.IO) {
            Library.playlistRepo?.deletePlaylists(playlist)
        }

    fun addSongIntoPlaylist(mediaId: String, playlistId: Long) =
        viewModelScope.launch(Dispatchers.IO) {
            Library.playlistRepo?.saveSongIntoPlaylist(playlistId, mediaId)
        }

    fun removeSongFromPlaylist(mediaId: String, playlistId: Long) =
        viewModelScope.launch(Dispatchers.IO) {
            Library.playlistRepo?.deleteSongFromPlaylist(playlistId, mediaId)
        }

    fun isContainSongInPlaylist(mediaId: String, playlistId: Long): Flow<Boolean> {
        return Library.playlistRepoFlow.flatMapLatest {
            it?.isSongContainInPlaylist(mediaId, playlistId) ?: flowOf(false)
        }
    }
}