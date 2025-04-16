package com.lalilu.lmusic.utils.sketch

import android.net.Uri
import androidx.media3.common.MediaItem
import com.github.panpf.sketch.annotation.WorkerThread
import com.github.panpf.sketch.fetch.FetchResult
import com.github.panpf.sketch.fetch.Fetcher
import com.github.panpf.sketch.request.RequestContext
import com.github.panpf.sketch.source.ByteArrayDataSource
import com.github.panpf.sketch.source.DataFrom
import com.lalilu.lmedia.extension.albumCoverUri
import com.lalilu.lmedia.extension.mediaUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun MediaItem.sketchUri(): Uri {
    return CoverItem(
        artworkUri = mediaMetadata.artworkUri,
        mediaUri = mediaId.toLongOrNull()?.mediaUri(),
        albumUrl = mediaId.toLongOrNull()?.albumCoverUri()
    ).toUri()
}

class SongCoverFetcher(
    private val requestContext: RequestContext,
) : Fetcher {

    @WorkerThread
    override suspend fun fetch(): Result<FetchResult> = withContext(Dispatchers.IO) {
        runCatching {
            val context = requestContext.sketch.context
            val uri = runCatching { Uri.parse(requestContext.request.uri.toString()) }
                .getOrNull()

            val coverItem = CoverItem.fromUri(uri)
                ?: throw IllegalArgumentException("Failed to fetch cover, uri is null")

            val inputStream =
                CoverFetcher.fetchByCoverItem(context = context, coverItem = coverItem)
                    ?: throw IllegalArgumentException("Failed to fetch cover, inputStream is null")

            FetchResult(
                dataSource = ByteArrayDataSource(
                    data = inputStream.readBytes(),
                    dataFrom = DataFrom.LOCAL
                ),
                mimeType = null
            )
        }
    }

    class Factory : Fetcher.Factory {
        override fun create(requestContext: RequestContext): Fetcher? {
            if (requestContext.request.uri.scheme != CoverItem.SCHEME) return null

            return SongCoverFetcher(requestContext = requestContext)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other != null && this::class == other::class
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }

        override fun toString(): String = "SongCoverFetcher"
    }
}