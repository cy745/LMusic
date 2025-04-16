package com.lalilu.lmusic.utils.coil.fetcher

import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.lalilu.lmedia.entity.LAlbum
import okio.buffer
import okio.source

class LAlbumFetcher private constructor(
    private val options: Options,
    private val album: LAlbum
) : BaseFetcher() {
    override suspend fun fetch(): FetchResult? {
        // 首先尝试从媒体库获取封面，若无则通过其内部的歌曲来获取
        val result = fetchMediaStoreCovers(options.context, album.coverUri)
            ?: album.songs.firstNotNullOfOrNull { fetchForSong(options.context, it) }

        return result?.let { stream ->
            SourceFetchResult(
                source = ImageSource(stream.source().buffer(), options.fileSystem),
                mimeType = null,
                dataSource = DataSource.DISK
            )
        }
    }

    class AlbumFactory : Fetcher.Factory<LAlbum> {
        override fun create(data: LAlbum, options: Options, imageLoader: ImageLoader) =
            LAlbumFetcher(options, data)
    }
}