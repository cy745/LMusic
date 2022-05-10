package com.lalilu.lmusic

import android.content.Context
import androidx.room.Room
import com.lalilu.lmusic.apis.NeteaseDataSource
import com.lalilu.lmusic.datasource.LMusicDataBase
import com.lalilu.lmusic.manager.LyricManager
import com.lalilu.lmusic.service.LMusicNotificationProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@ExperimentalCoroutinesApi
@InstallIn(SingletonComponent::class)
object LMusicHiltModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Config.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideLMusicDatabase(@ApplicationContext context: Context): LMusicDataBase {
        return Room.databaseBuilder(
            context,
            LMusicDataBase::class.java,
            "LMusic_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideNetWorkLyricService(retrofit: Retrofit): NeteaseDataSource {
        return retrofit.create(NeteaseDataSource::class.java)
    }
}

@Module
@ExperimentalCoroutinesApi
@InstallIn(SingletonComponent::class)
abstract class LMusicAbstractHiltModule {
    @Binds
    abstract fun bindLyricPush(pusher: LMusicNotificationProvider): LyricManager.LyricPusher
}