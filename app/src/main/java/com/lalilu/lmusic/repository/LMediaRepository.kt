package com.lalilu.lmusic.repository

import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LDictionary
import com.lalilu.lmedia.entity.LSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class LMediaRepository : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    val songsFlow = LMedia.getSongsFlow()
    val artistsFlow = LMedia.getArtistsFlow()
    val albumsFlow = LMedia.getAlbumsFlow()
    val genresFlow = LMedia.getGenresFlow()
    val dictionariesFlow = LMedia.getDictionariesFlow()

    val allSongsFlow = LMedia.getSongsFlow(false)
    val allDictionariesFlow = LMedia.getDictionariesFlow(false)

    fun getSongsFlow(num: Int, shuffle: Boolean = false): Flow<List<LSong>> =
        LMedia.getSongsFlow().mapLatest { it.let { if (shuffle) it.shuffled() else it }.take(num) }

    fun getSongs(num: Int = Int.MAX_VALUE, shuffle: Boolean = false): List<LSong> =
        LMedia.getSongs().let { if (shuffle) it.shuffled() else it }.take(num)

    fun requireSong(mediaId: String): LSong? =
        LMedia.getSongOrNull(mediaId)

    fun requireArtist(artistName: String): LArtist? =
        LMedia.getArtistOrNull(artistName)

    fun requireAlbum(albumId: String): LAlbum? =
        LMedia.getAlbumOrNull(albumId)

    fun requireDictionary(dictionaryId: String, blockFilter: Boolean = true): LDictionary? =
        LMedia.getDictionaryOrNull(dictionaryId, blockFilter)

    fun requireSongFlowById(mediaId: String?): Flow<LSong?> =
        LMedia.getSongFlowById(mediaId)
}