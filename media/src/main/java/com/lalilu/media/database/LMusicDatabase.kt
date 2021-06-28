package com.lalilu.media.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lalilu.media.dao.LMusicPlayListDao
import com.lalilu.media.dao.MusicDao
import com.lalilu.media.dao.PlayListDao
import com.lalilu.media.database.convertor.UriConvertor
import com.lalilu.media.entity.LMusicPlayList
import com.lalilu.media.entity.Music
import com.lalilu.media.entity.PlayList
import com.lalilu.media.entity.PlaylistMusicCrossRef

@Database(
    entities = [
        LMusicPlayList::class,
        Music::class,
        PlayList::class,
        PlaylistMusicCrossRef::class
    ],
    version = 1, exportSchema = false
)
@TypeConverters(
    value = [
        UriConvertor::class,
    ]
)
abstract class LMusicDatabase : RoomDatabase() {
    abstract fun playlistDao(): LMusicPlayListDao
    abstract fun playListDao(): PlayListDao
    abstract fun musicDao(): MusicDao

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