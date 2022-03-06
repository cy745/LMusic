package com.lalilu.lmusic.utils.fetcher

import androidx.media3.common.MediaItem
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.size.Size
import com.lalilu.lmusic.utils.sources.CoverSourceFactory
import javax.inject.Inject
import javax.inject.Singleton

fun MediaItem.getCoverFromMediaItem(): GetCoverFromMediaItem = GetCoverFromMediaItem(this)

class GetCoverFromMediaItem(val mediaItem: MediaItem)

@Singleton
class EmbeddedCoverFetcher @Inject constructor(
    private val coverSourceFactory: CoverSourceFactory
) : Fetcher<GetCoverFromMediaItem> {
    override suspend fun fetch(
        pool: BitmapPool,
        data: GetCoverFromMediaItem,
        size: Size,
        options: Options
    ): FetchResult {
        val bufferedSource = coverSourceFactory.getCover(data.mediaItem)
            ?: throw NullPointerException()
        return SourceResult(
            source = bufferedSource,
            mimeType = null,
            dataSource = DataSource.DISK
        )
    }

    override fun key(data: GetCoverFromMediaItem): String? {
        return "embedded_cover_${data.mediaItem.mediaId}"
    }
}