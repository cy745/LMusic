package com.lalilu.lmusic.apis.bean.netease

import com.lalilu.lmusic.apis.bean.NetworkLyric

data class KLyricSearchLRC(
    val version: Int,
    val lyric: String
) {

    override fun toString(): String {
        return "KLyricSearchLRC(version=$version, lyric='${lyric}')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KLyricSearchLRC

        if (version != other.version) return false
        if (lyric != other.lyric) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + lyric.hashCode()
        return result
    }
}

data class TLyricSearchLRC(
    val version: Int,
    val lyric: String
) {
    override fun toString(): String {
        return "TLyricSearchLRC(version=$version, lyric='${lyric}')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TLyricSearchLRC

        if (version != other.version) return false
        if (lyric != other.lyric) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + lyric.hashCode()
        return result
    }
}

data class LyricSearchLRC(
    val version: Int,
    val lyric: String
) {
    override fun toString(): String {
        return "LyricSearchLRC(version=$version, lyric='${lyric}')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LyricSearchLRC

        if (version != other.version) return false
        if (lyric != other.lyric) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + lyric.hashCode()
        return result
    }
}

data class LyricSearchResponse(
    val lrc: LyricSearchLRC?,
    val klyric: KLyricSearchLRC?,
    val tlyric: TLyricSearchLRC?,
    val code: Int
) : NetworkLyric {
    override fun toString(): String {
        return "LyricSearchResponse(lrc=$lrc, klyric=$klyric, tlyric=$tlyric, code=$code)"
    }

    override val mainLyric: String?
        get() = lrc?.lyric
    override val translateLyric: String?
        get() = tlyric?.lyric
    override val fromPlatform: Int
        get() = NetworkLyric.PLATFORM_NETEASE

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LyricSearchResponse

        if (lrc != other.lrc) return false
        if (klyric != other.klyric) return false
        if (tlyric != other.tlyric) return false
        if (code != other.code) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lrc?.hashCode() ?: 0
        result = 31 * result + (klyric?.hashCode() ?: 0)
        result = 31 * result + (tlyric?.hashCode() ?: 0)
        result = 31 * result + code
        return result
    }
}