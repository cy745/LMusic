package com.lalilu.lmusic.apis

import com.lalilu.lmusic.apis.bean.kugou.KugouLyric
import com.lalilu.lmusic.apis.bean.kugou.KugouLyricResponse
import com.lalilu.lmusic.apis.bean.kugou.KugouSearchSongResponse
import com.lalilu.lmusic.apis.bean.netease.SongDetailSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface KugouSongsSource {

    @GET("api/v3/search/song")
    suspend fun searchForSongs(
        @Query("keyword") keywords: String
    ): KugouSearchSongResponse?
}

interface KugouLyricSource {

    @GET("search")
    suspend fun searchForLyric(
        @Query("ver") ver: String = "1",
        @Query("man") man: String = "",
        @Query("client") client: String = "mobi",
        @Query("keyword") keyword: String = "",
        @Query("duration") duration: Long = 0L,
        @Query("hash") hash: String
    ): KugouLyricResponse?

    @GET("download")
    suspend fun getLyric(
        @Query("ver") ver: String = "1",
        @Query("client") client: String = "pc",
        @Query("id") id: String,
        @Query("accesskey") accessKey: String,
        @Query("fmt") fmt: String = "lrc",
        @Query("charset") charset: String = "utf8"
    ): KugouLyric?
}

class KugouDataSource(
    private val kugouSongsSource: KugouSongsSource,
    private val kugouLyricSource: KugouLyricSource
) : NetworkDataSource {

    override suspend fun searchForSongs(keywords: String): KugouSearchSongResponse? {
        return kugouSongsSource.searchForSongs(keywords)
    }

    override suspend fun searchForLyric(id: String): KugouLyric? {
        val lyricItem = kugouLyricSource
            .searchForLyric(hash = id)
            ?.getKugouLyricItem()
            ?: return null

        return kugouLyricSource.getLyric(id = lyricItem.id, accessKey = lyricItem.accesskey)
    }

    override suspend fun searchForDetail(ids: String): SongDetailSearchResponse? = null
}
