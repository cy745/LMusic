package com.lalilu.lmusic.apis

import com.lalilu.lmusic.apis.bean.SearchForLyric
import com.lalilu.lmusic.apis.bean.netease.LyricSearchResponse
import com.lalilu.lmusic.apis.bean.netease.SongSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 网易云的接口
 * 来源于 https://github.com/Binaryify/NeteaseCloudMusicApi
 */
interface NeteaseDataSource : SearchForLyric {
    /**
     * 查询歌曲
     */
    @GET("search")
    suspend fun searchForSong(
        @Query("keywords") keywords: String
    ): SongSearchResponse?

    /**
     * 查询歌词
     */
    @GET("lyric")
    override suspend fun searchForLyric(
        @Query("id") id: String
    ): LyricSearchResponse?
}