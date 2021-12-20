package com.lalilu.lmusic.database

import android.content.Context
import androidx.room.Room
import com.lalilu.lmusic.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LMusicDataModule {

    @Provides
    @Singleton
    fun provideLMusicDatabase(@ApplicationContext context: Context): LMusicDataBase {
        return Room.databaseBuilder(
            context,
            LMusicDataBase::class.java,
            "LMusic_database"
        ).addMigrations(LMusicDataBase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideSongDao(dataBase: LMusicDataBase): MSongDao {
        return dataBase.songDao()
    }

    @Provides
    @Singleton
    fun provideAlbumDao(dataBase: LMusicDataBase): MAlbumDao {
        return dataBase.albumDao()
    }

    @Provides
    @Singleton
    fun provideArtistDao(dataBase: LMusicDataBase): MArtistDao {
        return dataBase.artistDao()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(dataBase: LMusicDataBase): MPlaylistDao {
        return dataBase.playlistDao()
    }

    @Provides
    @Singleton
    fun provideSongDetailDao(dataBase: LMusicDataBase): MSongDetailDao {
        return dataBase.songDetailDao()
    }

    @Provides
    @Singleton
    fun provideRelationDao(dataBase: LMusicDataBase): MRelationDao {
        return dataBase.relationDao()
    }
}