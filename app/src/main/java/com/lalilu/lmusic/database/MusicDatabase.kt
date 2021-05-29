package com.lalilu.lmusic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lalilu.lmusic.dao.AlbumDao
import com.lalilu.lmusic.dao.SongDao
import com.lalilu.lmusic.entity.Album
import com.lalilu.lmusic.entity.Artist
import com.lalilu.lmusic.entity.Song
import com.lalilu.lmusic.utils.convertor.*

@Database(
    entities = [Song::class, Album::class, Artist::class],
    version = 3, exportSchema = false
)
@TypeConverters(
    value = [
        AlbumConvertor::class,
        SongConvertor::class,
        ArtistConvertor::class,
        AlbumListConvertor::class,
        ArtistListConvertor::class,
        UriConvertor::class
    ]
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao

    companion object {
        private const val DataBaseName = "MusicDataBase"

        @Volatile
        private var instance: MusicDatabase? = null

        fun getInstance(context: Context): MusicDatabase {
            instance ?: synchronized(MusicDatabase::class.java) {
                instance = instance ?: Room.databaseBuilder(
                    context,
                    MusicDatabase::class.java,
                    DataBaseName
                ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
            }
            return instance!!
        }
    }
}