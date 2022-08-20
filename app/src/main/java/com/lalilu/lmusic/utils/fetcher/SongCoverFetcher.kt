package com.lalilu.lmusic.utils.fetcher

import android.content.Context
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import com.lalilu.lmedia.entity.LSong
import okhttp3.Call
import okio.buffer
import okio.source

class SongCoverFetcher private constructor(
    private val context: Context,
    private val song: LSong,
    callFactory: Call.Factory,
    options: Options
) : CustomWithNetDataFetcher(callFactory = callFactory, options = options) {
    override suspend fun fetch(): FetchResult? = fetchForSong(context, song)?.let { stream ->
        SourceResult(
            source = ImageSource(stream.source().buffer(), context),
            mimeType = null,
            dataSource = DataSource.DISK
        )
    }

    class SongFactory(private val callFactory: Call.Factory) : Fetcher.Factory<LSong> {
        override fun create(data: LSong, options: Options, imageLoader: ImageLoader) =
            SongCoverFetcher(options.context, data, callFactory, options)
    }
}