package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.repository.LMediaRepository
import com.lalilu.component.extension.toState


class LMediaViewModel(
    private val lMediaRepo: LMediaRepository
) : ViewModel() {
    val allDictionaries = lMediaRepo.allDictionariesFlow
        .toState(emptyList(), viewModelScope)

    fun requireSong(mediaId: String): LSong? =
        lMediaRepo.requireSong(mediaId)

    fun requireArtist(artistName: String): LArtist? =
        lMediaRepo.requireArtist(artistName)

    fun requireAlbum(albumId: String): LAlbum? =
        lMediaRepo.requireAlbum(albumId)
}