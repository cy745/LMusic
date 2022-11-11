package com.lalilu.lmusic.repository

import androidx.lifecycle.asLiveData
import com.lalilu.lmedia.indexer.Library
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class LibraryRepository @Inject constructor() : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    val songsFlow = Library.getSongsFlow()
    val artistsFlow = Library.getArtistsFlow()
    val albumsFlow = Library.getAlbumsFlow()
    val genresFlow = Library.getGenresFlow()

    val songsLiveData = songsFlow.asLiveData(coroutineContext)
    val artistsLiveData = artistsFlow.asLiveData(coroutineContext)
    val albumsLiveData = albumsFlow.asLiveData(coroutineContext)
    val genresLiveData = genresFlow.asLiveData(coroutineContext)
}