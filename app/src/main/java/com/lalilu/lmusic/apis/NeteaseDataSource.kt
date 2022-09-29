package com.lalilu.lmusic.apis

import com.lalilu.lmusic.Config
import com.lalilu.lmusic.apis.bean.netease.NeteaseLyric
import com.lalilu.lmusic.apis.bean.netease.SongDetailSearchResponse
import com.lalilu.lmusic.apis.bean.netease.SongSearchResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Qualifier
import javax.inject.Singleton

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

@Module
@ExperimentalCoroutinesApi
@InstallIn(SingletonComponent::class)
object NeteaseHiltModule {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class NeteaseRetrofit

    @Provides
    @Singleton
    @NeteaseRetrofit
    fun provideNeteaseRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Config.BASE_NETEASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNeteaseService(@NeteaseRetrofit retrofit: Retrofit): NeteaseDataSource {
        return retrofit.create(NeteaseDataSource::class.java)
    }
}