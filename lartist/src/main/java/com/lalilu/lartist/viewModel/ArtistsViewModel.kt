package com.lalilu.lartist.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.component.extension.toState
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LArtist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class ArtistsViewModel : ViewModel() {
    private val artistIds = MutableStateFlow<List<String>>(emptyList())
    private val artistSource = LMedia.getFlow<LArtist>().combine(artistIds) { artists, ids ->
        if (ids.isEmpty()) return@combine artists
        artists.filter { artist -> artist.name in ids }
    }

    val artists = artistSource
        .toState(emptyList(), viewModelScope)
}