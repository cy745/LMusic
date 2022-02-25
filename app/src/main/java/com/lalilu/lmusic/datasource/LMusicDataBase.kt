package com.lalilu.lmusic.datasource

import android.content.Context
import android.net.Uri
import androidx.annotation.IntDef
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
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

@Entity(
    tableName = "song_in_playlist",
    foreignKeys = [
        ForeignKey(
            entity = MPlaylist::class,
            parentColumns = ["playlist_id"],
            childColumns = ["song_in_playlist_playlist_id"],
            onDelete = CASCADE
        )
    ],
    primaryKeys = [
        "song_in_playlist_playlist_id",
        "song_in_playlist_song_id"
    ]
)
data class SongInPlaylist(
    @ColumnInfo(name = "song_in_playlist_playlist_id")
    val playlistId: Long,
    @ColumnInfo(name = "song_in_playlist_song_id")
    val songId: Long,
    @ColumnInfo(name = "song_in_playlist_create_time")
    val time: Date = Date()
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
        LastPlayInfo::class,
        SongInPlaylist::class
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
    abstract fun songInPlaylistDao(): SongInPlaylistDao
}

@Dao
interface MPlaylistDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(vararg playlist: MPlaylist)

    @Update(entity = MPlaylist::class)
    fun update(vararg playlist: MPlaylist)

    @Delete(entity = MPlaylist::class)
    fun delete(vararg playlist: MPlaylist)

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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(vararg lastPlayInfo: LastPlayInfo)

    @Delete(entity = LastPlayInfo::class)
    fun delete(vararg lastPlayInfo: LastPlayInfo)

    @Query("SELECT * FROM last_play_info WHERE last_play_info_id = :id;")
    fun getById(id: Long): LastPlayInfo?
}

@Dao
interface SongInPlaylistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(vararg songInPlaylist: SongInPlaylist)

    @Delete(entity = SongInPlaylist::class)
    fun delete(vararg songInPlaylist: SongInPlaylist)

    @Query("SELECT * FROM song_in_playlist WHERE song_in_playlist_playlist_id = :playlistId;")
    fun getAllByPlaylistId(playlistId: Long): List<SongInPlaylist>
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
