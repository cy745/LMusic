package com.lalilu.lmusic.utils.fetcher

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.size.Size
import com.lalilu.R
import com.lalilu.lmusic.utils.EmbeddedDataUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

fun String.toEmbeddedLyricSource(): EmbeddedLyricSourceUri =
    EmbeddedLyricSourceUri(this)

class EmbeddedLyricSourceUri(val sourceStr: String? = null)

@Singleton
@SuppressLint("UseCompatLoadingForDrawables")
class EmbeddedLyricFetchers @Inject constructor(
    @ApplicationContext val mContext: Context
) : Fetcher<EmbeddedLyricSourceUri> {
    private val lrcIcon = mContext.resources.getDrawable(R.drawable.ic_lrc_fill, mContext.theme)

    override suspend fun fetch(
        pool: BitmapPool,
        data: EmbeddedLyricSourceUri,
        size: Size,
        options: Options
    ): FetchResult {
        val lyric = EmbeddedDataUtils.loadLyric(data.sourceStr)
        if (TextUtils.isEmpty(lyric)) throw NullPointerException()

        return DrawableResult(
            drawable = lrcIcon,
            isSampled = false,
            dataSource = DataSource.DISK
        )
    }

    override fun key(data: EmbeddedLyricSourceUri): String {
        return "embedded_lyric_${data.sourceStr}"
    }
}
