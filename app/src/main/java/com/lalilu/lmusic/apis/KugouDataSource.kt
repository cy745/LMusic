package com.lalilu.lmusic.apis

import com.lalilu.lmusic.apis.bean.kugou.KugouSearchSongResponse
import com.lalilu.lmusic.apis.bean.netease.SongDetailSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface KugouDataSource : SearchForLyric {

    @GET("api/v3/search/song")
    override suspend fun searchForSongs(@Query("keyword") keywords: String): KugouSearchSongResponse?

    override suspend fun searchForLyric(id: String, platform: Int): NetworkLyric? = null
    override suspend fun searchForDetail(ids: String): SongDetailSearchResponse? = null
}