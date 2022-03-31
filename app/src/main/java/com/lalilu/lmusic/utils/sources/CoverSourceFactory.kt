package com.lalilu.lmusic.utils.sources

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.media3.common.MediaItem
import com.lalilu.lmusic.datasource.extensions.getSongData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.BufferedSource
import okio.buffer
import okio.source
import org.jaudiotagger.audio.AudioFileIO
import java.io.ByteArrayInputStream
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

interface CoverSource {
    suspend fun loadCoverBytes(mediaItem: MediaItem): ByteArray?
    suspend fun loadCover(mediaItem: MediaItem): BufferedSource?
}

@Singleton
class CoverSourceFactory @Inject constructor(
    retrieverCoverSource: RetrieverCoverSource,
    audioTagCoverSource: AudioTagCoverSource
) : CoverSource {
    private val sources: MutableList<CoverSource> = ArrayList()

    init {
        /**
         * Android 7.1.1 以下使用MediaMetadataRetriever
         * 获取到的embeddedPicture转BufferedSource存在莫名阻塞的bug
         * 故 Android 7.1.1 以下优先使用AudioTag的获取方式
         */
        if (Build.VERSION.SDK_INT <= 25) {
            sources.add(audioTagCoverSource)
            sources.add(retrieverCoverSource)
        } else {
            sources.add(retrieverCoverSource)
            sources.add(audioTagCoverSource)
        }
    }

    override suspend fun loadCoverBytes(mediaItem: MediaItem): ByteArray? =
        withContext(Dispatchers.IO) {
            sources.forEach { source ->
                val bytes = source.loadCoverBytes(mediaItem)
                if (bytes != null) return@withContext bytes
            }
            return@withContext null
        }

    override suspend fun loadCover(mediaItem: MediaItem): BufferedSource? =
        withContext(Dispatchers.IO) {
            sources.forEach { source ->
                val cover = source.loadCover(mediaItem)
                if (cover != null) return@withContext cover
            }
            return@withContext null
        }
}

class RetrieverCoverSource @Inject constructor(
    @ApplicationContext private val mContext: Context
) : CoverSource {
    override suspend fun loadCoverBytes(mediaItem: MediaItem): ByteArray? {
        val mediaUri = mediaItem.mediaMetadata.mediaUri ?: return null
        var retriever: MediaMetadataRetriever? = null
        return try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(mContext, mediaUri)
            retriever.embeddedPicture ?: throw NullPointerException()
        } catch (e: NullPointerException) {
            null
        } finally {
            retriever?.close()
            retriever?.release()
        }
    }

    override suspend fun loadCover(mediaItem: MediaItem): BufferedSource? {
        val bytes = loadCoverBytes(mediaItem) ?: return null
        val source = ByteArrayInputStream(bytes).source()
        source.timeout().timeout(50, TimeUnit.MILLISECONDS)
        return source.buffer()
    }
}

class AudioTagCoverSource @Inject constructor() : CoverSource {
    override suspend fun loadCoverBytes(mediaItem: MediaItem): ByteArray? {
        val songData = mediaItem.mediaMetadata.getSongData() ?: return null
        val file = File(songData)
        if (!file.exists()) return null

        kotlin.runCatching {
            Logger.getLogger("org.jaudiotagger").level = Level.OFF
            return AudioFileIO.read(file)?.tag?.firstArtwork?.binaryData
        }
        return null
    }

    override suspend fun loadCover(mediaItem: MediaItem): BufferedSource? {
        val bytes = loadCoverBytes(mediaItem) ?: return null
        val source = ByteArrayInputStream(bytes).source()
        source.timeout().timeout(50, TimeUnit.MILLISECONDS)
        return source.buffer()
    }
}