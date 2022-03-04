package com.lalilu.lmusic.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.TextUtils
import android.util.LruCache
import coil.imageLoader
import coil.request.ImageRequest
import com.lalilu.R
import com.lalilu.lmusic.utils.fetcher.toEmbeddedCoverSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.BufferedSource
import okio.buffer
import okio.source
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import java.io.ByteArrayInputStream
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

object EmbeddedDataUtils {
    private val tagCache = LruCache<String, Tag?>(100)

    @Throws(Exception::class)
    fun getTag(songData: String?): Tag? {
        songData ?: return null
        Logger.getLogger("org.jaudiotagger").level = Level.OFF

        val tag = tagCache.get(songData)
        if (tag != null) return tag

        val file = File(songData)
        if (!file.exists()) return null
        return AudioFileIO.read(file).tag.also {
            tagCache.put(songData, it)
        }
    }

    fun loadCover(
        context: Context, mediaUri: Uri?,
        samplingValue: Int = -1,
        mutable: Boolean = true,
        onError: (Drawable?) -> Unit = {},
        onStart: (Drawable?) -> Unit = {},
        onSuccess: (Drawable) -> Unit
    ) {
        mediaUri ?: return
        val imageRequest = ImageRequest.Builder(context)
            .data(mediaUri.toEmbeddedCoverSource())
            .allowHardware(!mutable)
            .placeholder(R.drawable.ic_loader_line)
            .error(R.drawable.ic_error_warning_line)
            .target(onStart, onError, onSuccess)

        if (samplingValue > 0) imageRequest.size(samplingValue)
        context.imageLoader.enqueue(imageRequest.build())
    }

    fun loadCoverBufferSource(
        context: Context,
        mediaUri: Uri? = null,
        songData: String? = null
    ): BufferedSource? {
        if (songData == null && mediaUri == null) return null
        if (mediaUri != null) {
            var retriever: MediaMetadataRetriever? = null
            try {
                retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, mediaUri)
                retriever.embeddedPicture ?: throw NullPointerException()
                return ByteArrayInputStream(retriever.embeddedPicture)
                    .source().buffer()
            } catch (e: NullPointerException) {
            } finally {
                retriever?.close()
                retriever?.release()
            }
        }

        if (songData == null) return null
        val tag = getTag(songData) ?: return null
        tag.firstArtwork ?: return null
        return ByteArrayInputStream(tag.firstArtwork.binaryData)
            .source().buffer()
    }

    fun loadLyric(songData: String?): String? {
        songData ?: return null

        val audioTag = getTag(songData) ?: return null
        var lyric = audioTag.getFields(FieldKey.LYRICS)
            .run { if (isNotEmpty()) get(0).toString() else null }

        if (TextUtils.isEmpty(lyric)) {
            val path = songData.substring(0, songData.lastIndexOf('.')) + ".lrc"
            val lrcFile = File(path)

            if (lrcFile.exists()) lyric = lrcFile.readText()
        }
        if (TextUtils.isEmpty(lyric)) return null
        return lyric
    }

    suspend fun loadLyric(songData: String?, callback: (lyric: String?) -> Unit) =
        withContext(Dispatchers.IO) {
            callback(loadLyric(songData))
        }
}