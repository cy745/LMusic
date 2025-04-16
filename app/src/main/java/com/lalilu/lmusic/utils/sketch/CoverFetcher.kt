package com.lalilu.lmusic.utils.sketch

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.net.toUri
import com.blankj.utilcode.util.LogUtils
import com.lalilu.lmedia.wrapper.Taglib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

data class CoverItem(
    val mediaUri: Uri? = null,
    val artworkUri: Uri? = null,
    val albumUrl: Uri? = null
) {
    fun toUri(): Uri = Uri.Builder()
        .scheme(SCHEME)
        .path(PATH)
        .appendQueryParameter("media", "$mediaUri")
        .appendQueryParameter("artwork", "$artworkUri")
        .appendQueryParameter("album", "$albumUrl")
        .build()

    companion object {
        const val SCHEME = "cover"
        const val PATH = "items"

        fun fromUri(uri: Uri?): CoverItem? {
            if (uri == null || uri.scheme != SCHEME) return null

            val mediaUri = uri.getQueryParameter("media")?.toUri()
            val artworkUri = uri.getQueryParameter("artwork")?.toUri()
            val albumUrl = uri.getQueryParameter("album")?.toUri()

            return CoverItem(mediaUri, artworkUri, albumUrl)
        }
    }
}

object CoverFetcher {

    suspend fun fetchByCoverItem(
        context: Context,
        coverItem: CoverItem
    ): InputStream? {
        return fetchWithTaglib(context, coverItem.mediaUri)
            ?: fetchWithMediaStore(context, coverItem.artworkUri)
            ?: fetchWithMediaStore(context, coverItem.albumUrl)
            ?: fetchWithRetriever(context, coverItem.mediaUri)
    }

    suspend fun fetchWithTaglib(
        context: Context,
        mediaUri: Uri?
    ): InputStream? = withContext(Dispatchers.IO) {
        mediaUri ?: return@withContext null

        runCatching {
            context.contentResolver
                .openFileDescriptor(mediaUri, "r")
                ?.use { fileDescriptor ->
                    Taglib.getPictureWithFD(fileDescriptor.detachFd())
                        ?.inputStream()
                }
        }.getOrNull()
    }

    suspend fun fetchWithMediaStore(
        context: Context,
        itemUri: Uri?
    ): InputStream? {
        if (itemUri == null) return null

        return withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(itemUri)
            }.getOrNull()
        }
    }

    suspend fun fetchWithRetriever(
        context: Context,
        mediaUri: Uri?
    ): InputStream? {
        mediaUri ?: return null
        val retriever = MediaMetadataRetriever()

        return try {
            retriever.setDataSource(context, mediaUri)
            retriever.embeddedPicture?.inputStream()
        } catch (e: Exception) {
            LogUtils.e(mediaUri, e)
            null
        } finally {
            retriever.close()
            retriever.release()
        }
    }
}

