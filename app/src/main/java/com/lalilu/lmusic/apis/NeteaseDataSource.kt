package com.lalilu.lmusic.apis

import com.lalilu.lmusic.apis.bean.LyricSearchResponse
import com.lalilu.lmusic.apis.bean.SongSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NeteaseDataSource {
    @GET("search")
    suspend fun searchForSong(
        @Query("keywords") keywords: String
    ): SongSearchResponse?

    @GET("lyric")
    suspend fun searchForLyric(
        @Query("id") id: Long
    ): LyricSearchResponse?
}