package com.lalilu.lmusic

import android.content.Context
import androidx.room.Room
import com.funny.data_saver.core.DataSaverInterface
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
import javax.inject.Singleton

@Module
@ExperimentalCoroutinesApi
@InstallIn(SingletonComponent::class)
object LMusicHiltModule {


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