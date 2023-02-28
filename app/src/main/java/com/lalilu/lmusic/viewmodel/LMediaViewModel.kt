package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LDictionary
import com.lalilu.lmedia.entity.LSong


class LMediaViewModel : ViewModel() {
    val songs = LMedia.getSongsFlow()
    val genres = LMedia.getGenresFlow()
    val albums = LMedia.getAlbumsFlow()
    val artists = LMedia.getArtistsFlow()
    val dictionaries = LMedia.getDictionariesFlow()

    fun requireSong(mediaId: String): LSong? =
        LMedia.getSongOrNull(mediaId)

    fun requireArtist(artistName: String): LArtist? =
        LMedia.getArtistOrNull(artistName)

    fun requireAlbum(albumId: String): LAlbum? =
        LMedia.getAlbumOrNull(albumId)

    fun requireDictionary(dictionaryId: String): LDictionary? =
        LMedia.getDictionaryOrNull(dictionaryId)
}