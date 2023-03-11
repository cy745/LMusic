package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.repository.LMediaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class SongDetailViewModel(lMediaRepo: LMediaRepository) : ViewModel() {
    private val mediaIdFlow = MutableStateFlow<String?>(null)
    val song = mediaIdFlow.flatMapLatest { lMediaRepo.requireSongFlowById(it) }

    fun updateMediaId(mediaId: String) {
        mediaIdFlow.value = mediaId
    }
}