package com.lalilu.lmusic.apis.bean.netease

import com.lalilu.lmusic.apis.NetworkSearchResponse
import com.lalilu.lmusic.apis.NetworkSong
import com.lalilu.lmusic.apis.PLATFORM_NETEASE

data class SongSearchArtist(
    val id: Long,
    val name: String
)

data class SongSearchAlbum(
    val id: Long,
    val name: String,
    val publishTime: Long,
    val copyrightId: Long,
)

data class SongSearchResult(
    val songs: List<NeteaseSong>,
    val hasMore: Boolean,
    val songCount: Int
)

data class SongSearchResponse(
    val result: SongSearchResult,
    val code: Int
) : NetworkSearchResponse {
    override val songs: List<NetworkSong>
        get() = result.songs
}

data class NeteaseSong(
    val id: Long,
    val name: String,
    val alias: List<String>?,
    val artists: List<SongSearchArtist>,
    val album: SongSearchAlbum?,
    val duration: Long,
    val copyrightId: Long,
    val mark: Long
) : NetworkSong {
    override val songId: String
        get() = id.toString()
    override val songAlias: String?
        get() = alias?.takeIf { it.isNotEmpty() }
            ?.joinToString(separator = "/") { it }
    override val songTitle: String
        get() = name
    override val songArtist: String
        get() = artists.joinToString(separator = "/") { it.name }
    override val songAlbum: String
        get() = album?.name ?: "未知专辑"
    override val songDuration: Long
        get() = duration

    override val fromPlatform: Int
        get() = PLATFORM_NETEASE
}