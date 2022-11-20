package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.entity.Item
import com.lalilu.lmedia.repository.PlaylistRepository
import com.lalilu.lmusic.repository.LibraryRepository
import com.lalilu.lmusic.utils.extension.toState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
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
        .toState(emptyList(), viewModelScope)
    val artistsResult = libraryRepo.artistsFlow.searchFor(keywords)
        .toState(emptyList(), viewModelScope)
    val albumsResult = libraryRepo.albumsFlow.searchFor(keywords)
        .toState(emptyList(), viewModelScope)
    val genresResult = libraryRepo.genresFlow.searchFor(keywords)
        .toState(emptyList(), viewModelScope)
    val playlistResult = playlistRepo.getAllPlaylistFlow().searchFor(keywords)
        .toState(emptyList(), viewModelScope)

    private fun <T : Item> Flow<Collection<T>>.searchFor(keywords: Flow<Collection<String>>): Flow<List<T>> =
        combine(keywords) { items, keywordList ->
            if (keywordList.isEmpty()) return@combine emptyList()
            items.filter { item -> keywordList.all { item.getMatchStr().contains(it) } }
        }

    fun searchFor(str: String) {
        keywordStr.tryEmit(str)
    }
}

val LocalSearchVM = compositionLocalOf<SearchViewModel> {
    error("SearchViewModel hasn't not presented")
}