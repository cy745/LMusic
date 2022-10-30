package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.database.SongInPlaylist
import com.lalilu.lmedia.database.sort
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.indexer.Library
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.burnoutcrew.reorderable.ItemPosition
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class PlaylistDetailViewModel @Inject constructor() : ViewModel() {
    var songs by mutableStateOf(emptyList<LSong>())
    private var tempList by mutableStateOf(emptyList<SongInPlaylist>())

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
        val start = startIndex - 2
        val end = endIndex - 2
        if (start !in tempList.indices || end !in tempList.indices || start == end) return
        viewModelScope.launch(Dispatchers.IO) {
            Library.playlistRepo?.moveSongInPlaylist(
                tempList[start],
                tempList[end],
                start < end //
            )
        }
    }

    fun getPlaylistDetailById(playlistId: Long, scope: CoroutineScope) {
        songs = emptyList()
        tempList = emptyList()
        Library.playlistRepoFlow
            .flatMapLatest { repo ->
                repo?.getSongInPlaylists(playlistId) ?: flowOf(emptyList())
            }
            .mapLatest { it.sort(true) }
            .debounce(50)
            .onEach { list ->
                songs = list.mapNotNull { Library.getSongOrNull(it.mediaId) }
                tempList = list
            }
            .launchIn(scope)
    }

    fun getPlaylistFlow(playlistId: Long): Flow<LPlaylist?> {
        return Library.playlistRepoFlow.flatMapLatest { repo ->
            repo?.getPlaylistById(playlistId) ?: flowOf(null)
        }
    }
}