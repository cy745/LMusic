package com.lalilu.lmusic.apis.bean.netease

data class SongDetailAlbum(
    val id: Long,
    val name: String,
    val picUrl: String
)

data class SongDetailArtist(
    val id: Long,
    val name: String,
)

data class SongDetail(
    val id: Long,
    val name: String,
    val al: SongDetailAlbum,
    val ar: List<SongDetailArtist>,
    val pop: Float,
    val alia: List<String>
)

data class SongDetailSearchResponse(
    val songs: List<SongDetail>,
    val code: Int
)
