package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong


class LMediaViewModel : ViewModel() {
    val songs = LMedia.getSongsFlow()
    val genres = LMedia.getGenresFlow()
    val albums = LMedia.getAlbumsFlow()
    val artists = LMedia.getArtistsFlow()
    val dictionaries = LMedia.getDictionariesFlow()

    fun requireSong(mediaId: String): LSong? {
        return LMedia.getSongOrNull(mediaId)
    }
}