package com.lalilu.lmusic

import android.content.Context
import androidx.room.Room
import com.funny.data_saver.core.DataSaverInterface
import com.lalilu.lmusic.apis.KugouDataSource
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
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@ExperimentalCoroutinesApi
@InstallIn(SingletonComponent::class)
object LMusicHiltModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class NeteaseRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class KugouRetrofit

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
    @KugouRetrofit
    fun provideKugouRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Config.BASE_KUGOU_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNeteaseService(@NeteaseRetrofit retrofit: Retrofit): NeteaseDataSource {
        return retrofit.create(NeteaseDataSource::class.java)
    }

    @Provides
    @Singleton
    fun provideKugouService(@KugouRetrofit retrofit: Retrofit): KugouDataSource {
        return retrofit.create(KugouDataSource::class.java)
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .hostnameVerifier { _, _ -> true }
            .build()
        // TODO kugou的接口存在证书错误问题，暂时只能本机做忽略证书校验
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
    fun providesDataSaver(settingsDataStore: SettingsDataStore): DataSaverInterface {
        return DataSaverDataStorePreferences().apply {
            setDataStorePreferences(settingsDataStore.getDataStore())
        }
    }
}