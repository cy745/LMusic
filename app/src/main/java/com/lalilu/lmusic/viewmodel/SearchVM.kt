package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.component.extension.toState
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.Item
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LGenre
import com.lalilu.lmedia.entity.LSong
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import org.koin.core.annotation.Single

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@Single
class SearchVM : ViewModel() {
    private val _keywordStr = mutableStateOf("")
    var keywordStr: String
        get() = _keywordStr.value
        set(value) {
            _keywordStr.value = value
            _keywordsFlow.value = value
        }

    private val _keywordsFlow = MutableStateFlow("")
    private val keywords = _keywordsFlow.debounce(200).mapLatest {
        if (it.isEmpty()) return@mapLatest emptyList()
        it.trim().uppercase().split(' ')
    }

    val songsResult = LMedia.getFlow<LSong>()
        .searchFor(keywords)
        .toState(emptyList(), viewModelScope)
    val artistsResult = LMedia.getFlow<LArtist>()
        .searchFor(keywords)
        .toState(emptyList(), viewModelScope)
    val albumsResult = LMedia.getFlow<LAlbum>()
        .searchFor(keywords)
        .toState(emptyList(), viewModelScope)
    val genresResult = LMedia.getFlow<LGenre>()
        .searchFor(keywords)
        .toState(emptyList(), viewModelScope)

    private fun <T : Item> Flow<Collection<T>>.searchFor(keywords: Flow<Collection<String>>): Flow<List<T>> =
        combine(keywords) { items, keywordList ->
            if (keywordList.isEmpty()) return@combine emptyList()
            items.filter { item -> keywordList.all { item.getMatchStr().contains(it) } }
        }
}