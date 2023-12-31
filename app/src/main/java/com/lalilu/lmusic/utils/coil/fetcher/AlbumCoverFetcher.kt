package com.lalilu.lmusic.utils.coil.fetcher

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
    override suspend fun fetch(): FetchResult? {
        // 首先尝试从媒体库获取封面，若无则通过其内部的歌曲来获取
        val result = fetchMediaStoreCovers(context, album.coverUri)
            ?: album.songs.firstNotNullOfOrNull { fetchForSong(context, it) }

        return result?.let { stream ->
            SourceResult(
                source = ImageSource(stream.source().buffer(), context),
                mimeType = null,
                dataSource = DataSource.DISK
            )
        }
    }

    class AlbumFactory : Fetcher.Factory<LAlbum> {
        override fun create(data: LAlbum, options: Options, imageLoader: ImageLoader) =
            AlbumCoverFetcher(options.context, data)
    }
}