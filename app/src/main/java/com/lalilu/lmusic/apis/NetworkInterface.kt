package com.lalilu.lmusic.apis

import com.lalilu.lmusic.apis.bean.netease.SongDetailSearchResponse


interface SearchForLyric {
    suspend fun searchForSongs(keywords: String): NetworkSearchResponse?
    suspend fun searchForLyric(id: String, platform: Int): NetworkLyric?
    suspend fun searchForDetail(ids: String): SongDetailSearchResponse?
}

const val PLATFORM_NETEASE = 0
const val PLATFORM_QQMUISC = 1
const val PLATFORM_KUGOU = 2

interface NetworkLyric {
    val mainLyric: String?
    val translateLyric: String?

    val fromPlatform: Int
}

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

interface NetworkSearchResponse {
    val songs: List<NetworkSong>
}