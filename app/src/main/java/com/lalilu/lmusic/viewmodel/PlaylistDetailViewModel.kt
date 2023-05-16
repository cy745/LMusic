package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.database.sort
import com.lalilu.lmedia.repository.PlaylistRepository
import com.lalilu.lmusic.repository.LMediaRepository
import com.lalilu.lmusic.utils.extension.toCachedFlow
import com.lalilu.lmusic.utils.extension.toMutableState
import com.lalilu.lmusic.utils.extension.toState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ItemPosition

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistDetailViewModel(
    private val playlistRepo: PlaylistRepository,
    private val lMediaRepo: LMediaRepository
) : ViewModel() {
    private val playlistId = MutableStateFlow(0L)
    val playlist by playlistId.flatMapLatest { id -> playlistRepo.getPlaylistById(id) }
        .toState(viewModelScope)

    private var songsInPlaylistFlow =
        playlistId.flatMapLatest { id ->
            playlistRepo.getSongInPlaylistsFlowById(id).mapLatest { it.sort(true) }
        }.toCachedFlow()
//        .onEach { list ->
//            println("Flow Update: " + list.joinToString("<-") { "[${it.nextId}:${it.getLinkableId()}]" })
//        }

    @OptIn(FlowPreview::class)
    var songs by songsInPlaylistFlow
        .debounce(50)
        .mapLatest { list -> list.mapNotNull { lMediaRepo.requireSong(it.mediaId) } }
        .toMutableState(emptyList(), viewModelScope)
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
        val tempList = songsInPlaylistFlow.get() ?: return

        /**
         * 判断是否需要重新排序
         * 1. 列表内没有一个元素的nextId为null时，即没有末尾元素，需要重新排序
         * 2. 列表中出现重复的nextId时，也需要重新排序
         */
        val needReorder = tempList.all { it.nextId != null } ||
                tempList.map { it.nextId }.toSet().size != tempList.size

        val start = startIndex - 1
        val end = endIndex - 1
        if (start !in tempList.indices || end !in tempList.indices || start == end) return
        viewModelScope.launch(Dispatchers.IO) {
            if (needReorder) {
                playlistRepo.reorderSongInPlaylist(tempList)
            }
            playlistRepo.moveSongInPlaylist(
                tempList[start],
                tempList[end],
                start < end //
            )
        }
    }
}