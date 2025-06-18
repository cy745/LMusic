package com.lalilu.lmedia.lyric

import androidx.media3.common.MediaItem

interface LyricSource {
    suspend fun loadLyric(song: MediaItem): Pair<String, String?>?
    suspend fun hasLyric(song: MediaItem): Boolean
    suspend fun clearLyricCache(song: MediaItem) {}
}
