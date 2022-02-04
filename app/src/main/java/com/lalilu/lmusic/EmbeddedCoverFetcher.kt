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
import com.lalilu.lmusic.utils.EmbeddedDataUtils
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream
import javax.inject.Inject
import javax.inject.Singleton

fun Uri.toEmbeddedCoverSource(): EmbeddedCoverSourceUri = EmbeddedCoverSourceUri(this)

class EmbeddedCoverSourceUri(val sourceUri: Uri = Uri.EMPTY)

@Singleton
class EmbeddedCoverFetcher @Inject constructor() : Fetcher<EmbeddedCoverSourceUri> {
    override suspend fun fetch(
        pool: BitmapPool,
        data: EmbeddedCoverSourceUri,
        size: Size,
        options: Options
    ): FetchResult {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(options.context, data.sourceUri)

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

        val tag = EmbeddedDataUtils.getTag(data.sourceUri.path)
        tag?.firstArtwork ?: throw NullPointerException()
        val bufferedSource = ByteArrayInputStream(tag.firstArtwork.binaryData)
            .source().buffer()
        return SourceResult(
            source = bufferedSource,
            mimeType = null,
            dataSource = DataSource.DISK
        )
    }

    override fun key(data: EmbeddedCoverSourceUri): String {
        return "embedded_cover_${data.sourceUri}"
    }
}