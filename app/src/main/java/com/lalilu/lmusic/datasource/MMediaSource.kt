package com.lalilu.lmusic.datasource

import android.os.Bundle
import androidx.media3.common.MediaItem
import com.lalilu.lmusic.datasource.extensions.getArtistId
import com.lalilu.lmusic.manager.GlobalDataManager
import com.lalilu.lmusic.manager.HistoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class MMediaSource @Inject constructor(
    val mDataBase: MDataBase,
    private val mediaStoreHelper: MediaStoreHelper
) : BaseMediaSource(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    init {
        launch {
            mediaStoreHelper.mediaItems.collect {
                fillWithData(it)
                updateArtistFromMediaSource()
            }
        }
    }

    fun start() = launch {
        mediaStoreHelper.whenReady { recoverHistory() }
        mediaStoreHelper.start()
    }

    private suspend fun recoverHistory() {
        val lastPlaylist = HistoryManager.lastPlayedListIds?.mapNotNull {
            getItemById(ITEM_PREFIX + it)
        } ?: getChildren(ALL_ID) ?: emptyList()

        val lastPlayedItem = HistoryManager.lastPlayedId?.let {
            getItemById(ITEM_PREFIX + it)
        } ?: lastPlaylist.getOrNull(0)

        GlobalDataManager.currentMediaItem.emit(lastPlayedItem)
        GlobalDataManager.currentPlaylist.emit(lastPlaylist)
    }

    private suspend fun updateArtistFromMediaSource() = withContext(Dispatchers.IO) {
        getChildren(ARTIST_ID)?.map { mediaItem ->
            val artistText = mediaItem.mediaMetadata.artist.toString()
            val artistId = mediaItem.mediaMetadata.getArtistId().toString()

            artistText.split('/', '、').forEach { artist ->
                mDataBase.artistDao().saveArtist(artist, artistId)
            }
        }
    }


    override fun search(query: String, extras: Bundle): List<MediaItem> {
        return emptyList()
    }
}