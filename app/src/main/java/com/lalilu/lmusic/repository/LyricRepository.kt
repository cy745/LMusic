package com.lalilu.lmusic.repository

import com.lalilu.lmusic.utils.sources.LyricSourceFactory
import javax.inject.Inject

class LyricRepository @Inject constructor(
    private val lyricSource: LyricSourceFactory,
    private val lyricHelper: LyricHelper
) {

    fun getLyric(mediaId: String): Pair<String, String?>? {
        return null
    }

    fun bindLyric(mediaId: String, callback: (Pair<String, String?>?) -> Unit) {

    }

    fun requireUpdate() {

    }
}