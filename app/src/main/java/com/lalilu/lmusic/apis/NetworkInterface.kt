package com.lalilu.lmusic.apis

import com.lalilu.lmusic.apis.bean.netease.SongDetailSearchResponse


interface NetworkDataSource {
    suspend fun searchForSongs(keywords: String): NetworkSearchResponse?
    suspend fun searchForLyric(id: String): NetworkLyric?
    suspend fun searchForDetail(ids: String): SongDetailSearchResponse?
}

const val PLATFORM_NETEASE = 0
const val PLATFORM_QQMUISC = 1
const val PLATFORM_KUGOU = 2

val PLATFORM_TEXT = mapOf(
    PLATFORM_NETEASE to "163",
    PLATFORM_QQMUISC to "QQ",
    PLATFORM_KUGOU to "Kugou"
)

/**
 * 统一定义接口返回的歌词对象应该具有的属性
 */
interface NetworkLyric {
    val mainLyric: String?
    val translateLyric: String?

    val fromPlatform: Int
}

/**
 * 用于搜索歌曲，返回与关键词相关的歌曲列表的实体
 */
interface NetworkSearchResponse {
    val songs: List<NetworkSong>
}

/**
 * 统一定义接口返回的歌曲对象应该具有什么属性
 */
interface NetworkSong {
    val songId: String

    /**
     * 歌曲别名（类似于《XXX》片尾曲）
     */
    val songAlias: String?
    val songTitle: String
    val songArtist: String
    val songAlbum: String
    val songDuration: Long

    val fromPlatform: Int
}

interface NetworkSongDetail {
    val coverUrl: String
}