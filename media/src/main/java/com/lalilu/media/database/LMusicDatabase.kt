package com.lalilu.media.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lalilu.media.dao.LMusicAlbumDao
import com.lalilu.media.dao.LMusicMediaItemDao
import com.lalilu.media.database.convertor.AlbumConvertor
import com.lalilu.media.database.convertor.SongConvertor
import com.lalilu.media.database.convertor.UriConvertor
import com.lalilu.media.entity.LMusicAlbum
import com.lalilu.media.entity.LMusicMediaItem

@Database(
    entities = [LMusicMediaItem::class, LMusicAlbum::class],
    version = 5, exportSchema = false
)
@TypeConverters(
    value = [
        AlbumConvertor::class,
        SongConvertor::class,
        UriConvertor::class
    ]
)
abstract class LMusicDatabase : RoomDatabase() {
    abstract fun mediaItemDao(): LMusicMediaItemDao
    abstract fun albumDao(): LMusicAlbumDao

    companion object {
        private const val DataBaseName = "MusicDataBase"

        @Volatile
        private var instance: LMusicDatabase? = null

        fun getInstance(context: Context): LMusicDatabase {
            instance ?: synchronized(LMusicDatabase::class.java) {
                instance = instance ?: Room.databaseBuilder(
                    context,
                    LMusicDatabase::class.java,
                    DataBaseName
                ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
            }
            return instance!!
        }
    }
}