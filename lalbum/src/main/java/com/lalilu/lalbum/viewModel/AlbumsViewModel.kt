package com.lalilu.lalbum.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.component.extension.toState
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LAlbum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class AlbumsViewModel : ViewModel() {
    private val albumIds = MutableStateFlow<List<String>>(emptyList())
    private val albumSource = LMedia.getFlow<LAlbum>().combine(albumIds) { albums, ids ->
        if (ids.isEmpty()) return@combine albums
        albums.filter { album -> album.id in ids }
    }

    val albums = albumSource
        .toState(emptyList(), viewModelScope)
}