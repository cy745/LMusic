package com.lalilu.lmusic.utils.fetcher

import androidx.media3.common.MediaItem
import coil.bitmap.BitmapPool
import coil.decode.Options
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.size.Size
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * 结合了三个Fetcher，fetch排序为:
 * Network -> Embedded -> Album
 */
@Singleton
class MSongCoverFetcher @Inject constructor(
    private val httpUriFetcher: HttpUriFetcher,
    private val embeddedCoverFetcher: EmbeddedCoverFetcher,
    private val contentUriFetcher: ContentUriFetcher
) : Fetcher<MediaItem> {

    override suspend fun fetch(
        pool: BitmapPool,
        data: MediaItem,
        size: Size,
        options: Options
    ): FetchResult {
        val uri = data.mediaMetadata.artworkUri
        try {
            uri ?: throw CancellationException()
            if (httpUriFetcher.handles(uri)) {
                return httpUriFetcher.fetch(pool, uri, size, options)
            }
        } catch (e: Exception) {
        }

        try {
            if (embeddedCoverFetcher.handles(data)) {
                return embeddedCoverFetcher.fetch(pool, data, size, options)
            }
        } catch (e: Exception) {
        }

        uri ?: throw CancellationException()
        if (!contentUriFetcher.handles(uri)) throw CancellationException()
        return contentUriFetcher.fetch(pool, uri, size, options)
    }

    override fun key(data: MediaItem): String {
        return "mediaItem_${data.mediaId}"
    }
}