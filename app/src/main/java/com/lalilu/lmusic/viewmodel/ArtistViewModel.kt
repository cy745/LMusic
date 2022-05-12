package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import com.lalilu.lmusic.datasource.ARTIST_ID
import com.lalilu.lmusic.datasource.ARTIST_PREFIX
import com.lalilu.lmusic.datasource.MDataBase
import com.lalilu.lmusic.datasource.MMediaSource
import com.lalilu.lmusic.datasource.extensions.getArtistId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    val mediaSource: MMediaSource,
    val database: MDataBase
) : ViewModel() {
    val artists
        get() = database.artistDao().getAllArtistMapId()

    suspend fun getSongsByName(artistName: String): List<MediaItem> = withContext(Dispatchers.IO) {
        val artist = database.artistDao().getArtistByName(artistName)
        return@withContext artist.mapIds
            .mapNotNull {
                mediaSource.getChildren(ARTIST_PREFIX + it.originArtistId)
            }.flatten()
    }


    suspend fun updateArtistFromMediaSource() = withContext(Dispatchers.IO) {
        mediaSource.getChildren(ARTIST_ID)?.map { mediaItem ->
            val artistText = mediaItem.mediaMetadata.artist.toString()
            val artistId = mediaItem.mediaMetadata.getArtistId().toString()

            artistText.split('/', '、').forEach { artist ->
                database.artistDao().saveArtist(artist, artistId)
            }
        }
    }

    init {
        mediaSource.whenReady {
            if (!it) return@whenReady
            updateArtistFromMediaSource()
        }
    }
}