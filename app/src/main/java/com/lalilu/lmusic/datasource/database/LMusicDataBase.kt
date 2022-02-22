package com.lalilu.lmusic.datasource.database

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.room.*
import com.lalilu.lmusic.domain.entity.MPlaylist
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LMusicDataBaseModule {
    @Provides
    @Singleton
    fun provideLMusicDatabase(@ApplicationContext context: Context): LMusicDataBase {
        return Room.databaseBuilder(
            context,
            LMusicDataBase::class.java,
            "LMusic_database"
        ).fallbackToDestructiveMigration()
            .build()
    }
}

@Database(
    entities = [
        MPlaylist::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(
    UriConverter::class,
    DateConverter::class
)
abstract class LMusicDataBase : RoomDatabase() {
    abstract fun playlistDao(): MPlaylistDao
}

@Dao
interface MPlaylistDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(playlist: MPlaylist)

    @Delete(entity = MPlaylist::class)
    fun delete(playlist: MPlaylist)

    @Query("SELECT * FROM m_playlist;")
    fun getAll(): List<MPlaylist>

    @Query("SELECT * FROM m_playlist;")
    fun getAllLiveData(): LiveData<List<MPlaylist>>

    @Query("SELECT * FROM m_playlist ORDER BY playlist_create_time DESC;")
    fun getAllLiveDataSortByTime(): LiveData<List<MPlaylist>>

    @Query("SELECT * FROM m_playlist WHERE playlist_id = :id;")
    fun getById(id: Long): MPlaylist?
}

class UriConverter {
    @TypeConverter
    fun toString(uri: Uri): String {
        return uri.toString()
    }

    @TypeConverter
    fun toUri(text: String): Uri {
        return Uri.parse(text)
    }
}

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long): Date {
        return Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }
}
