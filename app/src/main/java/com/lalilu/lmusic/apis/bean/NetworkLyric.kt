package com.lalilu.lmusic.apis.bean


interface SearchForLyric {
    suspend fun searchForLyric(id: String): NetworkLyric?
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