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
import com.lalilu.lmusic.datasource.LMusicDataBase
import com.lalilu.lmusic.datasource.extensions.getSongData
import com.lalilu.lmusic.utils.EmbeddedDataUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@SuppressLint("UseCompatLoadingForDrawables")
class EmbeddedLyricFetchers @Inject constructor(
    @ApplicationContext val mContext: Context,
    val dataBase: LMusicDataBase
) : Fetcher<MediaItem> {
    private val lrcIcon = mContext.resources.getDrawable(R.drawable.ic_lrc_fill, mContext.theme)

    override suspend fun fetch(
        pool: BitmapPool,
        data: MediaItem,
        size: Size,
        options: Options
    ): FetchResult {
        var lyric = dataBase.persistLyricDao().getById(data.mediaId)?.lyric

        if (!TextUtils.isEmpty(lyric)) return DrawableResult(
            drawable = lrcIcon,
            isSampled = false,
            dataSource = DataSource.DISK
        )

        lyric = EmbeddedDataUtils.loadLyric(data.mediaMetadata.getSongData())
        if (TextUtils.isEmpty(lyric)) throw NullPointerException()

        return DrawableResult(
            drawable = lrcIcon,
            isSampled = false,
            dataSource = DataSource.DISK
        )
    }

    override fun key(data: MediaItem): String {
        return "embedded_lyric_${data.mediaId}"
    }
}
