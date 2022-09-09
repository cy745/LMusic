package com.lalilu.lmusic

import android.content.Context
import androidx.room.Room
import com.funny.data_saver.core.DataSaverInterface
import com.lalilu.lmusic.apis.NeteaseDataSource
import com.lalilu.lmusic.datasource.MDataBase
import com.lalilu.lmusic.repository.SettingsDataStore
import com.lalilu.lmusic.utils.DataSaverDataStorePreferences
import com.lalilu.lmusic.utils.DataSaverDataStorePreferences.Companion.setDataStorePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.Call
import okhttp3.OkHttpClient
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
    fun provideLMusicDatabase(@ApplicationContext context: Context): MDataBase {
        return Room.databaseBuilder(
            context,
            MDataBase::class.java,
            "LMusic_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideNetWorkLyricService(retrofit: Retrofit): NeteaseDataSource {
        return retrofit.create(NeteaseDataSource::class.java)
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(): Call.Factory {
        return OkHttpClient.Builder().build()
    }

    @Provides
    @Singleton
    fun providesDataSaver(settingsDataStore: SettingsDataStore): DataSaverInterface {
        return DataSaverDataStorePreferences().apply {
            setDataStorePreferences(settingsDataStore.getDataStore())
        }
    }
}