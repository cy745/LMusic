package com.lalilu.lmusic.repository

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

}