package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmusic.repository.LMediaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistsViewModel(
    private val lMediaRepo: LMediaRepository
) : ViewModel() {
    private val temp = MutableStateFlow(emptyList<LArtist>())
    val artists = mutableStateListOf<LArtist>()

    fun show(artistList: List<String>) {
        viewModelScope.launch {
            temp.emit(artistList.mapNotNull { lMediaRepo.requireArtist(artistName = it) })
        }
    }

    init {
        temp.flatMapLatest { source ->
            lMediaRepo.artistsFlow.mapLatest { source.ifEmpty { it } }
        }.onEach {
            artists.clear()
            artists.addAll(it)
        }.launchIn(viewModelScope)
    }
}