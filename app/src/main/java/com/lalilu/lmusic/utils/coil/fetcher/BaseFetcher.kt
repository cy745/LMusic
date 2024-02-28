package com.lalilu.lmusic.utils.coil.fetcher

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import coil.fetch.Fetcher
import com.blankj.utilcode.util.LogUtils
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.wrapper.Taglib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * 参照Auxio构造的Fetcher
 */
abstract class BaseFetcher : Fetcher {

    open suspend fun fetchForSong(context: Context, song: LSong): InputStream? {
        return fetchCoverByTaglib(context, song)
            ?: fetchMediaStoreCovers(context, song.albumCoverUri)
            ?: fetchMediaStoreCovers(context, song.artworkUri)
            ?: fetchCoverByRetriever(context, song)
    }

    protected suspend fun fetchCoverByRetriever(context: Context, song: LSong): InputStream? {
        val retriever = MediaMetadataRetriever()

        return try {
            retriever.setDataSource(context, song.uri)
            retriever.embeddedPicture?.inputStream()
        } catch (e: Exception) {
            LogUtils.e(song, e)
            null
        } finally {
            retriever.close()
            retriever.release()
        }
    }

    protected suspend fun fetchCoverByTaglib(context: Context, song: LSong): ByteArrayInputStream? {
        return runCatching { context.contentResolver.openFileDescriptor(song.uri, "r") }
            .getOrElse {
                LogUtils.e(song, it)
                null
            }?.use { fileDescriptor ->
                Taglib.getPictureWithFD(fileDescriptor.detachFd())
                    ?.inputStream()
            }
    }

    /**
     * 非音频文件Uri，而是已经缓存在MediaStore中的图片文件Uri
     */
    protected suspend fun fetchMediaStoreCovers(context: Context, uri: Uri?): InputStream? {
        uri ?: return null

        return withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(uri)
            }.getOrNull()
        }
    }
}