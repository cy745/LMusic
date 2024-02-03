package com.lalilu.lmusic.utils.coil.fetcher

import android.content.Context
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import com.lalilu.lmedia.entity.LSong
import okio.buffer
import okio.source

class LSongFetcher private constructor(
    private val context: Context,
    private val song: LSong
) : BaseFetcher() {

    override suspend fun fetch(): FetchResult? = fetchForSong(context, song)
        ?.let { stream ->
            SourceResult(
                source = ImageSource(stream.source().buffer(), context),
                mimeType = null,
                dataSource = DataSource.DISK
            )
        }

    class SongFactory : Fetcher.Factory<LSong> {
        override fun create(data: LSong, options: Options, imageLoader: ImageLoader): Fetcher? =
            LSongFetcher(options.context, data)
    }
}