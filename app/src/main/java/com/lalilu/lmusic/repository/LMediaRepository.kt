package com.lalilu.lmusic.repository

import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LDictionary
import com.lalilu.lmedia.entity.LGenre
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

    val songsFlow = LMedia.getFlow<LSong>()
    val artistsFlow = LMedia.getFlow<LArtist>()
    val albumsFlow = LMedia.getFlow<LAlbum>()
    val genresFlow = LMedia.getFlow<LGenre>()
    val dictionariesFlow = LMedia.getFlow<LDictionary>()

    val allSongsFlow = LMedia.getFlow<LSong>()
    val allDictionariesFlow = LMedia.getFlow<LDictionary>()

    fun getSongsFlow(num: Int, shuffle: Boolean = false): Flow<List<LSong>> =
        LMedia.getFlow<LSong>()
            .mapLatest { it.let { if (shuffle) it.shuffled() else it }.take(num) }

    fun getSongs(num: Int = Int.MAX_VALUE, shuffle: Boolean = false): List<LSong> =
        LMedia.get<LSong>().let { if (shuffle) it.shuffled() else it }.take(num)

    fun requireSong(mediaId: String): LSong? = LMedia.get(mediaId)
    fun requireArtist(artistName: String): LArtist? = LMedia.get(artistName)
    fun requireAlbum(albumId: String): LAlbum? = LMedia.get(albumId)

    fun requireDictionary(dictionaryId: String, blockFilter: Boolean = true): LDictionary? =
        LMedia.get(dictionaryId)

    fun requireSongFlowById(mediaId: String?): Flow<LSong?> =
        LMedia.getFlow(mediaId)

}