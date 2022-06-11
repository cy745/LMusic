package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import com.lalilu.lmusic.datasource.ARTIST_PREFIX
import com.lalilu.lmusic.datasource.MDataBase
import com.lalilu.lmusic.datasource.MMediaSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val mediaSource: MMediaSource,
    private val mDataBase: MDataBase
) : ViewModel() {
    val artists
        get() = mDataBase.artistDao().getAllArtistMapId()

    suspend fun getSongsByName(artistName: String): List<MediaItem> = withContext(Dispatchers.IO) {
        val artist = mDataBase.artistDao().getCustomMapArtists(artistName)
        return@withContext artist.mapIds
            .mapNotNull {
                mediaSource.getChildren(ARTIST_PREFIX + it.originArtistId)
            }.flatten()
    }
}