package com.lalilu.lmusic.repository

import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class LibraryRepository @Inject constructor() : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    val songsFlow = LMedia.getSongsFlow()
    val artistsFlow = LMedia.getArtistsFlow()
    val albumsFlow = LMedia.getAlbumsFlow()
    val genresFlow = LMedia.getGenresFlow()

    fun getSongsFlow(num: Int, shuffle: Boolean = false): Flow<List<LSong>> =
        songsFlow.mapLatest { it.let { if (shuffle) it.shuffled() else it }.take(num) }

    fun getSongs(num: Int, shuffle: Boolean = false): List<LSong> = LMedia.getSongs(num, shuffle)
}