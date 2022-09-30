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
//            ?: fetchExoplayerCover(context, song)
            ?: fetchMediaStoreCovers(context, song)
            ?: song.album?.let { fetchForAlbum(context, it) }
    }

    open suspend fun fetchForAlbum(context: Context, album: LAlbum): InputStream? {
        return fetchMediaStoreCovers(context, album)
            ?: fetchAospMetadataCovers(context, album)
//            ?: fetchExoplayerCover(context, album)
    }

    private fun fetchAospMetadataCovers(context: Context, album: LAlbum): InputStream? {
        var result: InputStream? = null
        for (song in album.songs) {
            result = fetchAospMetadataCovers(context, song)
            if (result != null) break
        }
        return result
    }

//    private suspend fun fetchExoplayerCover(context: Context, album: LAlbum): InputStream? {
//        var result: InputStream? = null
//        for (song in album.songs) {
//            result = fetchExoplayerCover(context, song)
//            if (result != null) break
//        }
//        return result
//    }

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

//    private suspend fun fetchExoplayerCover(context: Context, song: LSong): InputStream? {
//        val future = MetadataRetriever.retrieveMetadata(context, MediaItem.fromUri(song.uri))
//
//        // future.get is a blocking call that makes us spin until the future is done.
//        // This is bad for a co-routine, as it prevents cancellation and by extension
//        // messes with the image loading process and causes frustrating bugs.
//        // To fix this we wrap this around in a withContext call to make it suspend and make
//        // sure that the runner can do other coroutines.
//        @Suppress("BlockingMethodInNonBlockingContext")
//        val tracks =
//            withContext(Dispatchers.IO) {
//                try {
//                    future.get()
//                } catch (e: Exception) {
//                    null
//                }
//            }
//
//        if (tracks == null || tracks.isEmpty) {
//            // Unrecognized format. This is expected, as ExoPlayer only supports a
//            // subset of formats.
//            return null
//        }
//
//        // The metadata extraction process of ExoPlayer results in a dump of all metadata
//        // it found, which must be iterated through.
//        val metadata = tracks[0].getFormat(0).metadata
//
//        if (metadata == null || metadata.length() == 0) {
//            // No (parsable) metadata. This is also expected.
//            return null
//        }
//
//        var stream: ByteArrayInputStream? = null
//
//        for (i in 0 until metadata.length()) {
//            // We can only extract pictures from two tags with this method, ID3v2's APIC or
//            // Vorbis picture comments.
//            val pic: ByteArray?
//            val type: Int
//
//            when (val entry = metadata.get(i)) {
//                is ApicFrame -> {
//                    pic = entry.pictureData
//                    type = entry.pictureType
//                }
//                is PictureFrame -> {
//                    pic = entry.pictureData
//                    type = entry.pictureType
//                }
//                else -> continue
//            }
//
//            if (type == MediaMetadata.PICTURE_TYPE_FRONT_COVER) {
//                LogUtils.d("Front cover found")
//                stream = ByteArrayInputStream(pic)
//                break
//            } else if (stream == null) {
//                stream = ByteArrayInputStream(pic)
//            }
//        }
//
//        return stream
//    }

    private suspend fun fetchMediaStoreCovers(context: Context, album: LAlbum): InputStream? {
        val uri = album.coverUri ?: return null

        // Eliminate any chance that this blocking call might mess up the loading process
        @Suppress("BlockingMethodInNonBlockingContext")
        return fetchMediaStoreCovers(context, uri)
    }

    private suspend fun fetchMediaStoreCovers(context: Context, song: LSong): InputStream? {
        val uri = song._albumCoverUri ?: return null

        // Eliminate any chance that this blocking call might mess up the loading process
        @Suppress("BlockingMethodInNonBlockingContext")
        return fetchMediaStoreCovers(context, uri)
    }

    private suspend fun fetchMediaStoreCovers(context: Context, uri: Uri?): InputStream? {
        uri ?: return null

        // Eliminate any chance that this blocking call might mess up the loading process
        @Suppress("BlockingMethodInNonBlockingContext")
        return withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(uri)
            }.getOrNull()
        }
    }
}