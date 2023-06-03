package com.lalilu.lmusic.api.lrcshare

data class SongResult(
    val id: Int,
    val song: String,
    val artist: String,
    val album: String?,
    val album_artist: String?,
    val comment: String?,
    val composer: String?,
    val cover: String?,
    val disc: Int?,
    val genre: String?,
    val track: Int?,
    val writer: String?,
    val year: String?
)