package com.lalilu.lmusic.utils.coil.fetcher

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.blankj.utilcode.util.LogUtils
import com.lalilu.lmedia.extension.EXTERNAL_CONTENT_URI
import com.lalilu.lmedia.wrapper.Taglib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream
import java.io.InputStream

class MediaItemFetcher(
    private val options: Options,
    private val item: MediaItem
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val songUri = EXTERNAL_CONTENT_URI.buildUpon()
            .appendEncodedPath(item.mediaId)
            .build()
            ?: return null

        val stream = when (item.mediaMetadata.mediaType) {
            MediaMetadata.MEDIA_TYPE_MUSIC -> {
                fetchCoverByTaglib(options.context, songUri)
                    ?: fetchCoverByRetriever(options.context, songUri)
                    ?: fetchMediaStoreCovers(options.context, item.mediaMetadata.artworkUri)
            }

            else -> fetchMediaStoreCovers(options.context, item.mediaMetadata.artworkUri)
        } ?: return null

        return SourceFetchResult(
            source = ImageSource(stream.source().buffer(), options.fileSystem),
            mimeType = null,
            dataSource = DataSource.DISK
        )
    }

    private suspend fun fetchCoverByRetriever(
        context: Context,
        songUri: Uri
    ): InputStream? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(context, songUri)
            retriever.embeddedPicture?.inputStream()
        } catch (e: Exception) {
            LogUtils.e(songUri, e)
            null
        } finally {
            retriever.close()
            retriever.release()
        }
    }

    private suspend fun fetchCoverByTaglib(
        context: Context,
        songUri: Uri
    ): ByteArrayInputStream? = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openFileDescriptor(songUri, "r")
        }.getOrElse {
            LogUtils.e(songUri, it)
            null
        }?.use { fileDescriptor ->
            Taglib.getPictureWithFD(fileDescriptor.detachFd())
                ?.inputStream()
        }
    }

    /**
     * 非音频文件Uri，而是已经缓存在MediaStore中的图片文件Uri
     */
    private suspend fun fetchMediaStoreCovers(context: Context, uri: Uri?): InputStream? {
        uri ?: return null

        return withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(uri)
            }.getOrNull()
        }
    }


    class MediaItemFetcherFactory : Fetcher.Factory<MediaItem> {
        override fun create(data: MediaItem, options: Options, imageLoader: ImageLoader): Fetcher =
            MediaItemFetcher(options, data)
    }
}