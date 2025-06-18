package com.lalilu.lmedia.lyric.impl

import android.content.Context
import androidx.media3.common.MediaItem
import com.lalilu.lmedia.lyric.LyricSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single


@Single
fun provideLyricSource(
    context: Context,
): LyricSource {
    return LyricSourceCombined(
        LyricSourceEmbedded(context),
        LyricSourceLocal(),
    )
}

class LyricSourceCombined(
    private vararg val sources: LyricSource
) : LyricSource {
    override suspend fun loadLyric(
        song: MediaItem
    ): Pair<String, String?>? = withContext(Dispatchers.IO) {
        sources.firstNotNullOfOrNull { it.loadLyric(song) }
    }

    override suspend fun hasLyric(song: MediaItem): Boolean {
        return sources.any { it.hasLyric(song) }
    }
}
