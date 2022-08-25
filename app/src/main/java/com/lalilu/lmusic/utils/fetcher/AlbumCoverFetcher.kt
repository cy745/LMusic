package com.lalilu.lmusic.utils.fetcher

import android.content.Context
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import com.lalilu.lmedia.entity.LAlbum
import okio.buffer
import okio.source

class AlbumCoverFetcher private constructor(
    private val context: Context,
    private val album: LAlbum
) : BaseFetcher() {
    override suspend fun fetch(): FetchResult? = fetchForAlbum(context, album)?.let { stream ->
        SourceResult(
            source = ImageSource(stream.source().buffer(), context),
            mimeType = null,
            dataSource = DataSource.DISK
        )
    }

    class AlbumFactory : Fetcher.Factory<LAlbum> {
        override fun create(data: LAlbum, options: Options, imageLoader: ImageLoader) =
            AlbumCoverFetcher(options.context, data)
    }
}