package com.lalilu.lmusic.apis

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

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

data class SongSearchSong(
    val id: Long,
    val name: String,
    val artists: List<SongSearchArtist>,
    val album: SongSearchAlbum,
    val duration: Long,
    val copyrightId: Long,
    val mark: Long
)

data class SongSearchResult(
    val songs: List<SongSearchSong>,
    val hasMore: Boolean,
    val songCount: Int
)

data class SongSearchResponse(
    val result: SongSearchResult,
    val code: Int
)

interface NetworkLyric {
    @GET("search")
    fun searchForSong(@Query("keywords") keywords: String):
            Call<SongSearchResponse>
}