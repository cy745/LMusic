package com.lalilu.lmusic.apis

import com.lalilu.lmusic.apis.bean.netease.NeteaseLyric
import com.lalilu.lmusic.apis.bean.netease.SongDetailSearchResponse
import com.lalilu.lmusic.apis.bean.netease.SongSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 网易云的接口
 * 来源于 https://github.com/Binaryify/NeteaseCloudMusicApi
 */
interface NeteaseDataSource : NetworkDataSource {
    /**
     * 查询歌曲
     */
    @GET("search")
    override suspend fun searchForSongs(
        @Query("keywords") keywords: String
    ): SongSearchResponse?

    /**
     * 查询歌词
     */
    @GET("lyric")
    override suspend fun searchForLyric(
        @Query("id") id: String
    ): NeteaseLyric?

    /**
     * 查询歌曲详细信息
     */
    @GET("song/detail")
    override suspend fun searchForDetail(
        @Query("ids") ids: String
    ): SongDetailSearchResponse?
}