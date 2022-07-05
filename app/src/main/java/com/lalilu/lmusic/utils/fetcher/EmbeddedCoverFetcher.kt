package com.lalilu.lmusic.utils.fetcher

import androidx.media3.common.MediaItem
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.size.Size
import com.lalilu.lmusic.datasource.extensions.getSongData
import com.lalilu.lmusic.utils.sources.CoverSourceFactory
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmbeddedCoverFetcher @Inject constructor(
    private val coverSourceFactory: CoverSourceFactory
) : Fetcher<MediaItem> {
    override fun handles(data: MediaItem): Boolean {
        return data.localConfiguration?.uri != null ||
                data.mediaMetadata.getSongData() != null
    }

    override suspend fun fetch(
        pool: BitmapPool,
        data: MediaItem,
        size: Size,
        options: Options
    ): FetchResult {
        val bufferedSource = coverSourceFactory.loadCover(data)
            ?: throw CancellationException()
        return SourceResult(
            source = bufferedSource,
            mimeType = null,
            dataSource = DataSource.DISK
        )
    }

    override fun key(data: MediaItem): String? {
        return "embedded_cover_${data.mediaId}"
    }
}