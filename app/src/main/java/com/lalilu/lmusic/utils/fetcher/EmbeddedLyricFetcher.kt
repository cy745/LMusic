package com.lalilu.lmusic.utils.fetcher

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import androidx.media3.common.MediaItem
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.size.Size
import com.lalilu.R
import com.lalilu.lmusic.utils.sources.LyricSourceFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

fun MediaItem.getLyric() = LyricRequest(this)
class LyricRequest(val mediaItem: MediaItem)

@Singleton
@SuppressLint("UseCompatLoadingForDrawables")
class EmbeddedLyricFetcher @Inject constructor(
    @ApplicationContext private val mContext: Context,
    private val lyricSourceFactory: LyricSourceFactory
) : Fetcher<LyricRequest> {
    private val lrcIcon = mContext.resources.getDrawable(R.drawable.ic_lrc_fill, mContext.theme)

    override suspend fun fetch(
        pool: BitmapPool,
        data: LyricRequest,
        size: Size,
        options: Options
    ): FetchResult {
        val pair = lyricSourceFactory.getLyric(data.mediaItem)
        if (TextUtils.isEmpty(pair?.first)) throw NullPointerException()

        return DrawableResult(
            drawable = lrcIcon,
            isSampled = false,
            dataSource = DataSource.DISK
        )
    }

    override fun key(data: LyricRequest): String {
        return "embedded_lyric_${data.mediaItem.mediaId}"
    }
}
