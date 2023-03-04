package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmusic.repository.LMediaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumsViewModel(
    private val lMediaRepo: LMediaRepository
) : ViewModel() {
    private val temp = MutableStateFlow(emptyList<LAlbum>())
    val albums = mutableStateListOf<LAlbum>()

    fun show(albumList: List<String>) {
        viewModelScope.launch {
            temp.emit(albumList.mapNotNull { lMediaRepo.requireAlbum(it) })
        }
    }

    init {
        temp.flatMapLatest { source ->
            lMediaRepo.albumsFlow.mapLatest { source.ifEmpty { it } }
        }.onEach {
            albums.clear()
            albums.addAll(it)
        }.launchIn(viewModelScope)
    }
}