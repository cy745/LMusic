package com.lalilu.lmusic.apis.bean

import com.lalilu.lmusic.apis.bean.netease.SongDetailSearchResponse
import com.lalilu.lmusic.apis.bean.netease.SongSearchResponse


interface SearchForLyric {
    suspend fun searchForSong(keywords: String): SongSearchResponse?
    suspend fun searchForLyric(id: String): NetworkLyric?
    suspend fun searchForDetail(ids: String): SongDetailSearchResponse?
}

interface NetworkLyric {
    companion object {
        const val PLATFORM_NETEASE = 0
        const val PLATFORM_QQMUISC = 1
    }

    val mainLyric: String?
    val translateLyric: String?
    val fromPlatform: Int
}