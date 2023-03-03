package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.database.sort
import com.lalilu.lmedia.entity.SongInPlaylist
import com.lalilu.lmedia.repository.PlaylistRepository
import com.lalilu.lmusic.utils.extension.toMutableState
import com.lalilu.lmusic.utils.extension.toState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ItemPosition

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PlaylistDetailViewModel(
    private val playlistRepo: PlaylistRepository
) : ViewModel() {
    private val playlistId = MutableStateFlow(0L)
    private var tempList by mutableStateOf(emptyList<SongInPlaylist>())
    val playlist by playlistId.flatMapLatest { id -> playlistRepo.getPlaylistById(id) }
        .toState(viewModelScope)
    var songs by playlistId.flatMapLatest { id ->
        playlistRepo.getSongInPlaylists(id)
            .mapLatest { it.sort(true) }
            .debounce(50)
            .onEach { tempList = it }
            .mapLatest { list -> list.mapNotNull { LMedia.getSongOrNull(it.mediaId) } }
    }.toMutableState(emptyList(), viewModelScope)
        private set

    fun loadPlaylistById(id: Long) {
        playlistId.value = id
    }

    fun onMoveItem(from: ItemPosition, to: ItemPosition) {
        val toIndex = songs.indexOfFirst { it.id == to.key }
        val fromIndex = songs.indexOfFirst { it.id == from.key }

        if (toIndex < 0 || fromIndex < 0) return
        songs = songs.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
    }

    fun canDragOver(draggedOver: ItemPosition, dragging: ItemPosition): Boolean =
        songs.any { draggedOver.key == it.id }

    fun onDragEnd(startIndex: Int, endIndex: Int) {
        val start = startIndex - 1
        val end = endIndex - 1
        if (start !in tempList.indices || end !in tempList.indices || start == end) return
        viewModelScope.launch(Dispatchers.IO) {
            playlistRepo.moveSongInPlaylist(
                tempList[start],
                tempList[end],
                start < end //
            )
        }
    }
}