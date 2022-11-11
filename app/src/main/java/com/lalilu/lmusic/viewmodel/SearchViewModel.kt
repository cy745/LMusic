package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.entity.Item
import com.lalilu.lmedia.repository.PlaylistRepository
import com.lalilu.lmusic.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    libraryRepo: LibraryRepository,
    playlistRepo: PlaylistRepository
) : ViewModel() {
    val keywordStr = MutableStateFlow("")
    private val keywords = keywordStr.debounce(200).mapLatest {
        if (it.isEmpty()) return@mapLatest emptyList()
        it.trim().uppercase().split(' ')
    }

    val songsResult = libraryRepo.songsFlow.searchFor(keywords)
        .asLiveData(viewModelScope.coroutineContext)

    val artistsResult = libraryRepo.artistsFlow.searchFor(keywords)
        .asLiveData(viewModelScope.coroutineContext)

    val albumsResult = libraryRepo.albumsFlow.searchFor(keywords)
        .asLiveData(viewModelScope.coroutineContext)

    val genresResult = libraryRepo.genresFlow.searchFor(keywords)
        .asLiveData(viewModelScope.coroutineContext)

    val playlistResult = playlistRepo.getAllPlaylistFlow().searchFor(keywords)
        .asLiveData(viewModelScope.coroutineContext)

    private fun <T : Item> Flow<Collection<T>>.searchFor(keywords: Flow<Collection<String>>): Flow<List<T>> =
        combine(keywords) { items, keyword ->
            if (keyword.isEmpty()) return@combine emptyList()
            items.filter { item -> keyword.any { item.getMatchStr().contains(it) } }
        }

    fun searchFor(str: String) {
        keywordStr.tryEmit(str)
    }
}