package com.lalilu.lmusic.datasource

import android.content.Context
import android.net.Uri
import androidx.annotation.IntDef
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

const val TYPE_SONG = 0
const val TYPE_PLAYLIST = 1
const val TYPE_ALBUM = 2

@IntDef(TYPE_SONG, TYPE_PLAYLIST, TYPE_ALBUM)
@Retention(AnnotationRetention.SOURCE)
annotation class EntityType

@Entity(
    tableName = "last_play_info",
    primaryKeys = [
        "last_play_info_id",
        "last_play_info_type"
    ]
)
data class LastPlayInfo(
    @ColumnInfo(name = "last_play_info_id")
    val id: Long,
    @EntityType
    @ColumnInfo(name = "last_play_info_type")
    val type: Int,
    @ColumnInfo(name = "last_play_info_time")
    val time: Date = Date(),
)

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
        MPlaylist::class,
        LastPlayInfo::class
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
    abstract fun lastPlayInfoDao(): LastPlayInfoDao
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

@Dao
interface LastPlayInfoDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(lastPlayInfo: LastPlayInfo)

    @Delete(entity = LastPlayInfo::class)
    fun delete(lastPlayInfo: LastPlayInfo)

    @Query("SELECT * FROM last_play_info WHERE last_play_info_id = :id;")
    fun getById(id: Long): LastPlayInfo?
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
