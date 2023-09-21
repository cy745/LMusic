package com.lalilu.lmusic.repository

import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LDictionary
import com.lalilu.lmedia.entity.LGenre
import com.lalilu.lmedia.entity.LSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

@Deprecated("用处不大，慢慢移除，转为直接使用LMedia")
class LMediaRepository : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    val songsFlow = LMedia.getFlow<LSong>()
    val artistsFlow = LMedia.getFlow<LArtist>()
    val albumsFlow = LMedia.getFlow<LAlbum>()

    val allSongsFlow = LMedia.getFlow<LSong>()
    val allDictionariesFlow = LMedia.getFlow<LDictionary>()

    fun requireSong(mediaId: String): LSong? = LMedia.get(mediaId)
    fun requireArtist(artistName: String): LArtist? = LMedia.get(artistName)
    fun requireAlbum(albumId: String): LAlbum? = LMedia.get(albumId)

    fun requireDictionary(dictionaryId: String, blockFilter: Boolean = true): LDictionary? =
        LMedia.get(dictionaryId)

    fun requireSongFlowById(mediaId: String?): Flow<LSong?> =
        LMedia.getFlow(mediaId)
}