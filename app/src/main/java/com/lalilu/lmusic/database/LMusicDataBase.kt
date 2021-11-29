package com.lalilu.lmusic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = false
)
@TypeConverters(UriConverter::class)
abstract class LMusicDataBase : RoomDatabase() {
    abstract fun songDao(): MSongDao
    abstract fun albumDao(): MAlbumDao
    abstract fun artistDao(): MArtistDao
    abstract fun playlistDao(): MPlaylistDao
    abstract fun songDetailDao(): MSongDetailDao
    abstract fun relationDao(): MRelationDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE m_playlist ADD COLUMN playlist_title TEXT NOT NULL DEFAULT '';")
                database.execSQL("ALTER TABLE m_playlist ADD COLUMN playlist_cover_uri TEXT NOT NULL DEFAULT '';")
            }
        }

        @Volatile
        private var Instance: LMusicDataBase? = null

        fun getInstance(applicationContext: Context?): LMusicDataBase {
            Instance = Instance ?: synchronized(this) {
                Instance ?: Room.databaseBuilder(
                    applicationContext!!,
                    LMusicDataBase::class.java,
                    "LMusic_database"
                ).addMigrations(MIGRATION_1_2).fallbackToDestructiveMigration().build()
            }
            return Instance!!
        }
    }
}

