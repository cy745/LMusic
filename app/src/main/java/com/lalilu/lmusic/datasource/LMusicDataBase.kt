package com.lalilu.lmusic.datasource

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.lalilu.lmusic.datasource.entity.MPlaylist
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.*
import javax.inject.Singleton

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
    val mediaId: String,
    @ColumnInfo(name = "song_in_playlist_create_time")
    val time: Date = Date()
)

@Entity(tableName = "network_lyric")
data class PersistLyric(
    @PrimaryKey
    @ColumnInfo(name = "network_lyric_media_id")
    val mediaId: String,
    @ColumnInfo(name = "network_lyric_lyric")
    val lyric: String,
    @ColumnInfo(name = "network_lyric_tlyric")
    val tlyric: String? = null
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
        SongInPlaylist::class,
        PersistLyric::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(
    UriConverter::class,
    DateConverter::class
)
abstract class LMusicDataBase : RoomDatabase() {
    abstract fun playlistDao(): MPlaylistDao
    abstract fun songInPlaylistDao(): SongInPlaylistDao
    abstract fun persistLyricDao(): PersistLyricDao
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

    @Query("SELECT * FROM m_playlist ORDER BY playlist_create_time DESC;")
    fun getAllLiveDataSortByTime(): LiveData<List<MPlaylist>>

    @Query("SELECT * FROM m_playlist WHERE playlist_id = :id;")
    fun getById(id: Long): MPlaylist?
}

@Dao
interface SongInPlaylistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(vararg songInPlaylist: SongInPlaylist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(songInPlaylists: List<SongInPlaylist>)

    @Delete(entity = SongInPlaylist::class)
    fun delete(vararg songInPlaylist: SongInPlaylist)

    @Query("SELECT * FROM song_in_playlist WHERE song_in_playlist_playlist_id = :playlistId;")
    fun getAllByPlaylistId(playlistId: Long): List<SongInPlaylist>
}

@Dao
interface PersistLyricDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(vararg persistLyric: PersistLyric)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(persistLyricList: List<PersistLyric>)

    @Query("SELECT * FROM network_lyric WHERE network_lyric_media_id = :id;")
    fun getById(id: String): PersistLyric?

    @Delete(entity = PersistLyric::class)
    fun delete(vararg persistLyric: PersistLyric)
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
