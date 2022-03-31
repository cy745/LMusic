package com.lalilu.lmusic.utils.fetcher

import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.size.Size
import com.blankj.utilcode.util.EncodeUtils
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream
import javax.inject.Inject
import javax.inject.Singleton

fun String.getBase64Cover(): Base64Cover = Base64Cover(this)

class Base64Cover(val base64: String)

@Singleton
class Base64CoverFetcher @Inject constructor() : Fetcher<Base64Cover> {
    override suspend fun fetch(
        pool: BitmapPool,
        data: Base64Cover,
        size: Size,
        options: Options
    ): FetchResult {
        val bytes = EncodeUtils.base64Decode(data.base64)
            ?: throw NullPointerException()
        return SourceResult(
            source = ByteArrayInputStream(bytes).source().buffer(),
            mimeType = null,
            dataSource = DataSource.DISK
        )
    }

    override fun key(data: Base64Cover): String? {
        if (data.base64.length < 20) return null
        return "base64_cover_${data.base64.dropLast(20)}"
    }
}