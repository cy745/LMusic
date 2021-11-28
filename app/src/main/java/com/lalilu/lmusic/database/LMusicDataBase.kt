package com.lalilu.lmusic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lalilu.lmusic.database.dao.*
import com.lalilu.lmusic.domain.entity.*

@Database(
    entities = [
        MSong::class,
        MAlbum::class,
        MArtist::class,
        MPlaylist::class,
        MSongDetail::class,
        ArtistSongCrossRef::class,
        PlaylistSongCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(UriConverter::class)
abstract class LMusicDataBase : RoomDatabase() {
    abstract fun songDao(): MSongDao
    abstract fun albumDao(): MAlbumDao
    abstract fun artistDao(): MArtistDao
    abstract fun playlistDao(): MPlaylistDao
    abstract fun songDetailDao(): MSongDetailDao

    companion object {
        @Volatile
        private var Instance: LMusicDataBase? = null

        fun getInstance(applicationContext: Context?): LMusicDataBase {
            Instance = Instance ?: synchronized(this) {
                Instance ?: Room.databaseBuilder(
                    applicationContext!!,
                    LMusicDataBase::class.java,
                    "LMusic_database"
                ).build()
            }
            return Instance!!
        }
    }
}

