package com.lalilu.lmusic.utils.fetcher

import android.net.Uri
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.size.Size
import com.lalilu.lmusic.utils.EmbeddedDataUtils
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
        val bufferedSource = EmbeddedDataUtils.loadCoverBufferSource(
            options.context, data.sourceUri, data.sourceUri.path
        ) ?: throw NullPointerException()
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