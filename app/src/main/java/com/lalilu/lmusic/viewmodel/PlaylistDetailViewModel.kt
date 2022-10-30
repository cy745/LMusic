package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.database.SongInPlaylist
import com.lalilu.lmedia.database.sort
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
class PlaylistDetailViewModel @Inject constructor() : ViewModel() {
    var songs by mutableStateOf(emptyList<LSong>())
    private var tempList by mutableStateOf(emptyList<SongInPlaylist>())
    private val playlistIdFlow = MutableStateFlow(-1L)

    fun onMoveItem(from: ItemPosition, to: ItemPosition) {
        val toIndex = songs.indexOfFirst { it.id == to.key }
        val fromIndex = songs.indexOfFirst { it.id == from.key }

        if (toIndex < 0 || fromIndex < 0) return
        songs = songs.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
    }

    fun canDragOver(pos: ItemPosition) = songs.any { pos.key == it.id }

    fun onDragEnd(startIndex: Int, endIndex: Int) {
        val start = startIndex - 1
        val end = endIndex - 1
        if (start !in tempList.indices || end !in tempList.indices || start == end) return
        viewModelScope.launch(Dispatchers.IO) {
            Library.playlistRepo?.moveSongInPlaylist(tempList[start], tempList[end], start > end)
        }
    }

    init {
        Library.playlistRepoFlow
            .flatMapLatest { repo ->
                playlistIdFlow.flatMapLatest {
                    repo?.getSongInPlaylists(it) ?: flowOf(emptyList())
                }
            }
            .mapLatest { it.sort(false) }
            .debounce(50)
            .onEach { list ->
                songs = list.mapNotNull { Library.getSongOrNull(it.mediaId) }
                tempList = list
            }
            .launchIn(viewModelScope)
    }

    fun getPlaylistDetailById(playlistId: Long) = viewModelScope.launch {
        playlistIdFlow.emit(playlistId)
    }
}