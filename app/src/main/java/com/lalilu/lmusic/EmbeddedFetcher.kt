package com.lalilu.lmusic

import android.media.MediaMetadataRetriever
import android.net.Uri
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.size.Size
import okio.buffer
import okio.source
import org.jaudiotagger.audio.mp3.MP3File
import java.io.ByteArrayInputStream

class EmbeddedFetcher : Fetcher<Uri> {
    override suspend fun fetch(
        pool: BitmapPool,
        data: Uri,
        size: Size,
        options: Options
    ): FetchResult {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(options.context, data)

        try {
            retriever.embeddedPicture ?: throw NullPointerException()
            val bufferedSource = ByteArrayInputStream(retriever.embeddedPicture)
                .source().buffer()
            return SourceResult(
                source = bufferedSource,
                mimeType = null,
                dataSource = DataSource.DISK
            )
        } catch (e: NullPointerException) {
        }

        val mp3File = MP3File(data.path)
        mp3File.tag.firstArtwork ?: throw NullPointerException()
        val bufferedSource = ByteArrayInputStream(mp3File.tag.firstArtwork.binaryData)
            .source().buffer()
        return SourceResult(
            source = bufferedSource,
            mimeType = null,
            dataSource = DataSource.DISK
        )
    }

    override fun key(data: Uri): String {
        return "embedded_$data"
    }
}