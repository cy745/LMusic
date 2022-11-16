package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.database.sort
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ItemPosition
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlistRepo: PlaylistRepository
) : ViewModel() {
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

    fun canDragOver(draggedOver: ItemPosition, dragging: ItemPosition) :Boolean = playlists.any { draggedOver.key == it._id }

    fun onDragEnd(startIndex: Int, endIndex: Int) {
        val start = startIndex - 1
        val end = endIndex - 1
        if (start !in tempList.indices || end !in tempList.indices || start == end) return
        viewModelScope.launch(Dispatchers.IO) {
            playlistRepo.movePlaylist(tempList[start], tempList[end], start > end)
        }
    }

    init {
        playlistRepo.getAllPlaylistWithDetailFlow().mapLatest { it.sort(false) }.debounce(50)
            .onEach {
                playlists = it
                tempList = it
            }.launchIn(viewModelScope)
    }

    fun createNewPlaylist(title: String) = viewModelScope.launch(Dispatchers.IO) {
        playlistRepo.savePlaylist(LPlaylist(_title = title))
    }

    fun removePlaylists(playlist: List<LPlaylist>) = viewModelScope.launch(Dispatchers.IO) {
        playlistRepo.deletePlaylists(playlist)
    }

    fun addSongIntoPlaylist(playlistId: Long, mediaId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            playlistRepo.saveSongIntoPlaylist(playlistId, mediaId)
        }

    fun addSongsIntoPlaylists(playlists: List<LPlaylist>, songs: List<LSong>) =
        viewModelScope.launch(Dispatchers.IO) {
            playlistRepo.saveSongsIntoPlaylists(playlists, songs)
        }

    fun removeSongFromPlaylist(mediaId: String, playlistId: Long) =
        viewModelScope.launch(Dispatchers.IO) {
            playlistRepo.deleteSongFromPlaylist(playlistId, mediaId)
        }

    fun removeSongsFromPlaylist(songs: List<LSong>, playlist: LPlaylist) =
        viewModelScope.launch(Dispatchers.IO) {
            playlistRepo.deleteSongsFromPlaylist(songs, playlist)
        }

    fun isContainSongInPlaylist(mediaId: String, playlistId: Long): Flow<Boolean> {
        return playlistRepo.isSongContainInPlaylist(mediaId, playlistId)
    }
}