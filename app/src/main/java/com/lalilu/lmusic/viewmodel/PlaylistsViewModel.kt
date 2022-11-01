package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.database.sort
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.indexer.Library
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ItemPosition
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class PlaylistsViewModel @Inject constructor() : ViewModel() {
    var playlists by mutableStateOf(emptyList<LPlaylist>())
    private var tempList by mutableStateOf(emptyList<LPlaylist>())

    fun onMovePlaylist(from: ItemPosition, to: ItemPosition) {
        val toIndex = playlists.indexOfFirst { it._id == to.key }
        val fromIndex = playlists.indexOfFirst { it._id == from.key }

        if (toIndex < 0 || fromIndex < 0) return
        playlists = playlists.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
    }

    fun canDragOver(pos: ItemPosition) = playlists.any { pos.key == it._id }

    fun onDragEnd(startIndex: Int, endIndex: Int) {
        val start = startIndex - 2
        val end = endIndex - 2
        if (start !in tempList.indices || end !in tempList.indices || start == end) return
        viewModelScope.launch(Dispatchers.IO) {
            Library.playlistRepo?.movePlaylist(tempList[start], tempList[end], start > end)
        }
    }


    init {
        Library.playlistRepoFlow
            .flatMapLatest { it?.getAllPlaylistWithDetailFlow() ?: flowOf(emptyList()) }
            .mapLatest { it.sort(false) }
            .debounce(50)
            .onEach {
                playlists = it
                tempList = it
            }
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

    fun addSongIntoPlaylist(playlistId: Long, mediaId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            Library.playlistRepo?.saveSongIntoPlaylist(playlistId, mediaId)
        }

    fun addSongsIntoPlaylists(playlists: List<LPlaylist>, songs: List<LSong>) =
        viewModelScope.launch(Dispatchers.IO) {
            Library.playlistRepo?.saveSongsIntoPlaylists(playlists, songs)
        }

    fun removeSongFromPlaylist(mediaId: String, playlistId: Long) =
        viewModelScope.launch(Dispatchers.IO) {
            Library.playlistRepo?.deleteSongFromPlaylist(playlistId, mediaId)
        }

    fun removeSongsFromPlaylist(songs: List<LSong>, playlist: LPlaylist) =
        viewModelScope.launch(Dispatchers.IO) {
            Library.playlistRepo?.deleteSongsFromPlaylist(songs, playlist)
        }

    fun isContainSongInPlaylist(mediaId: String, playlistId: Long): Flow<Boolean> {
        return Library.playlistRepoFlow.flatMapLatest {
            it?.isSongContainInPlaylist(mediaId, playlistId) ?: flowOf(false)
        }
    }
}