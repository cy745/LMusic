package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.database.sort
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.repository.FavoriteRepository
import com.lalilu.lmedia.repository.PlaylistRepository
import com.lalilu.lmusic.utils.extension.toCachedFlow
import com.lalilu.lmusic.utils.extension.toMutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ItemPosition

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class PlaylistsViewModel constructor(
    private val playlistRepo: PlaylistRepository,
    private val favoriteRepo: FavoriteRepository
) : ViewModel() {
    private val playlistFlow = playlistRepo.getAllPlaylistWithDetailFlow()
        .mapLatest { it.sort(false) }
        .toCachedFlow()

    var playlists by playlistFlow.debounce(50)
        .toMutableState(emptyList(), viewModelScope)

    fun onMovePlaylist(from: ItemPosition, to: ItemPosition) {
        val toIndex = playlists.indexOfFirst { it._id == to.key }
        val fromIndex = playlists.indexOfFirst { it._id == from.key }

        if (toIndex < 0 || fromIndex < 0) return
        playlists = playlists.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
    }

    fun canDragOver(draggedOver: ItemPosition, dragging: ItemPosition): Boolean =
        playlists.any { draggedOver.key == it._id }

    fun onDragEnd(startIndex: Int, endIndex: Int) {
        val tempList = playlistFlow.get() ?: return

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
                playlistRepo.reorderPlaylist(tempList)
            }
            playlistRepo.movePlaylist(
                tempList[start],
                tempList[end],
                start > end
            )
        }
    }

    fun addToFavorite(song: LSong) = viewModelScope.launch(Dispatchers.IO) {
        favoriteRepo.addToFavorite(song)
    }

    fun removeFromFavorite(song: LSong) = viewModelScope.launch(Dispatchers.IO) {
        favoriteRepo.removeFromFavorite(song)
    }

    fun checkIsFavorite(song: LSong): Flow<Boolean> {
        return favoriteRepo.checkIsFavorite(song.id)
    }

    fun createNewPlaylist(title: String) = viewModelScope.launch(Dispatchers.IO) {
        playlistRepo.savePlaylist(LPlaylist(_title = title))
    }

    fun removePlaylists(playlist: List<LPlaylist>) = viewModelScope.launch(Dispatchers.IO) {
        playlistRepo.deletePlaylists(playlist)
    }

    fun addSongsIntoPlaylists(pIds: List<Long>, mediaIds: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            playlistRepo.saveSongsIntoPlaylists(pIds, mediaIds)
        }
    }

    fun removeSongsFromPlaylist(songs: List<LSong>, playlist: LPlaylist) =
        viewModelScope.launch(Dispatchers.IO) {
            playlistRepo.deleteSongsFromPlaylist(songs, playlist)
        }
}