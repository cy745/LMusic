package com.lalilu.media.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lalilu.media.dao.LMusicAlbumDao
import com.lalilu.media.dao.LMusicMediaDao
import com.lalilu.media.dao.LMusicPlayListDao
import com.lalilu.media.database.convertor.AlbumConvertor
import com.lalilu.media.database.convertor.MediaIdTreeSetConvertor
import com.lalilu.media.database.convertor.SongConvertor
import com.lalilu.media.database.convertor.UriConvertor
import com.lalilu.media.entity.LMusicAlbum
import com.lalilu.media.entity.LMusicMedia
import com.lalilu.media.entity.LMusicPlayList

@Database(
    entities = [LMusicMedia::class, LMusicAlbum::class, LMusicPlayList::class],
    version = 4, exportSchema = false
)
@TypeConverters(
    value = [
        MediaIdTreeSetConvertor::class,
        AlbumConvertor::class,
        SongConvertor::class,
        UriConvertor::class,
    ]
)
abstract class LMusicDatabase : RoomDatabase() {
    abstract fun playlistDao(): LMusicPlayListDao
    abstract fun mediaItemDao(): LMusicMediaDao
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