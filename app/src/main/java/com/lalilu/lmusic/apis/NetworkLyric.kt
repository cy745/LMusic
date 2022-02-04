package com.lalilu.lmusic.apis

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NetworkLyric {
    @GET("search")
    fun searchForSong(
        @Query("keywords") keywords: String,
    ): Call<ResponseBody?>?
}