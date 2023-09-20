package com.lalilu.lmusic.utils.coil.fetcher

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import coil.fetch.Fetcher
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * 参照Auxio构造的Fetcher
 */
abstract class BaseFetcher : Fetcher {

    open suspend fun fetchForSong(context: Context, song: LSong): InputStream? {
        return fetchAospMetadataCovers(context, song)
            ?: fetchMediaStoreCovers(context, song)
            ?: song.album?.let { fetchForAlbum(context, it) }
    }

    open suspend fun fetchForAlbum(context: Context, album: LAlbum): InputStream? {
        return fetchMediaStoreCovers(context, album)
            ?: fetchAospMetadataCovers(context, album)
    }

    private fun fetchAospMetadataCovers(context: Context, album: LAlbum): InputStream? {
        var result: InputStream? = null
        for (song in album.songs) {
            result = fetchAospMetadataCovers(context, song)
            if (result != null) break
        }
        return result
    }

    private fun fetchAospMetadataCovers(context: Context, song: LSong): InputStream? {
        MediaMetadataRetriever().apply {
            // This call is time-consuming but it also doesn't seem to hold up the main thread,
            // so it's probably fine not to wrap it.rmt
            setDataSource(context, song.uri)

            // Get the embedded picture from MediaMetadataRetriever, which will return a full
            // ByteArray of the cover without any compression artifacts.
            // If its null [i.e there is no embedded cover], than just ignore it and move on
            return embeddedPicture?.let { ByteArrayInputStream(it) }.also { release() }
        }
    }

    private suspend fun fetchMediaStoreCovers(context: Context, album: LAlbum): InputStream? {
        val uri = album.coverUri ?: return null

        return fetchMediaStoreCovers(context, uri)
    }

    private suspend fun fetchMediaStoreCovers(context: Context, song: LSong): InputStream? {
        val uri = song.albumCoverUri

        return fetchMediaStoreCovers(context, uri)
    }

    private suspend fun fetchMediaStoreCovers(context: Context, uri: Uri?): InputStream? {
        uri ?: return null

        return withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(uri)
            }.getOrNull()
        }
    }
}