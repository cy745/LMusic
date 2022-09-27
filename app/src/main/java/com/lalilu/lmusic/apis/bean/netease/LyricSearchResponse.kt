package com.lalilu.lmusic.apis.bean.netease

import com.lalilu.lmusic.apis.bean.NetworkLyric

data class KLyricSearchLRC(
    val version: Int,
    val lyric: String
)

data class TLyricSearchLRC(
    val version: Int,
    val lyric: String
)

data class LyricSearchLRC(
    val version: Int,
    val lyric: String
)

data class LyricSearchResponse(
    val lrc: LyricSearchLRC?,
    val klyric: KLyricSearchLRC?,
    val tlyric: TLyricSearchLRC?,
    val code: Int
) : NetworkLyric {

    override val mainLyric: String?
        get() = lrc?.lyric
    override val translateLyric: String?
        get() = tlyric?.lyric
    override val fromPlatform: Int
        get() = NetworkLyric.PLATFORM_NETEASE

}