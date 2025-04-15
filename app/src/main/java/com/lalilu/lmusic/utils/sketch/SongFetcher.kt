package com.lalilu.lmusic.utils.sketch

import android.net.Uri
import androidx.media3.common.MediaItem
import com.github.panpf.sketch.annotation.WorkerThread
import com.github.panpf.sketch.fetch.FetchResult
import com.github.panpf.sketch.fetch.Fetcher
import com.github.panpf.sketch.request.RequestContext
import com.github.panpf.sketch.source.ByteArrayDataSource
import com.github.panpf.sketch.source.DataFrom

fun Uri.isSketchUri(): Boolean {
    return this.scheme == "media"
}

fun MediaItem.sketchUri(): Uri {
    return Uri.Builder()
        .scheme("media")
        .path(mediaId)
        .encodedQuery("artworkUri=${this.mediaMetadata.artworkUri}")
        .build()
}

class SongCoverFetcher(
    private val mediaId: String,
    private val artworkUri: Uri?
) : Fetcher {

    @WorkerThread
    override suspend fun fetch(): Result<FetchResult> = runCatching {
        FetchResult(
            dataSource = ByteArrayDataSource(
                data = byteArrayOf(),
                dataFrom = DataFrom.LOCAL,
            ),
            mimeType = "mimeType"
        )
    }

    class Factory : Fetcher.Factory {
        override fun create(requestContext: RequestContext): Fetcher? {
            val uri = requestContext.request.uri
            if (uri.scheme != "media") return null

            return SongCoverFetcher(
                mediaId = uri.pathSegments.firstOrNull() ?: "",
                artworkUri = null
            )
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