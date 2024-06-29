package com.lalilu.lmusic.utils.coil.fetcher

import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.lalilu.lmedia.entity.LSong
import okio.buffer
import okio.source

class LSongFetcher private constructor(
    private val options: Options,
    private val song: LSong
) : BaseFetcher() {

    override suspend fun fetch(): FetchResult? = fetchForSong(options.context, song)
        ?.let { stream ->
            SourceFetchResult(
                source = ImageSource(stream.source().buffer(), options.fileSystem),
                mimeType = null,
                dataSource = DataSource.DISK
            )
        }

    class SongFactory : Fetcher.Factory<LSong> {
        override fun create(data: LSong, options: Options, imageLoader: ImageLoader): Fetcher? =
            LSongFetcher(options, data)
    }
}