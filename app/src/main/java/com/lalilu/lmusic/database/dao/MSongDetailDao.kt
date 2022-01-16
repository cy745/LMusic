package com.lalilu.lmusic.database.dao

import android.net.Uri
import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE
import com.lalilu.lmusic.domain.entity.MSongDetail

@Dao
interface MSongDetailDao {
    @Query("SELECT * FROM m_song_detail")
    fun getAll(): List<MSongDetail>

    @Query("SELECT * FROM m_song_detail WHERE song_id = :songId")
    fun getById(songId: Long): MSongDetail?

    @Query("SELECT * FROM m_song_detail WHERE song_id = :songId")
    fun getByIdStr(songId: String): MSongDetail?

    @Insert(onConflict = IGNORE)
    fun save(songDetail: MSongDetail)

    @Update(entity = MSongDetail::class)
    fun updateCoverUri(update: MSongDetailUpdateCoverUri)

    @Update(entity = MSongDetail::class)
    fun updateLyric(update: MSongDetailUpdateLyric)

    @Query("DELETE FROM m_song_detail;")
    suspend fun deleteAll()
}

data class MSongDetailUpdateLyric(
    @ColumnInfo(name = "song_id")
    val songId: Long,
    @ColumnInfo(name = "song_lyric")
    val songLyric: String,
    @ColumnInfo(name = "song_size")
    val songSize: Long,
    @ColumnInfo(name = "song_data")
    val songData: String
)

data class MSongDetailUpdateCoverUri(
    @ColumnInfo(name = "song_id")
    val songId: Long,
    @ColumnInfo(name = "song_cover_uri")
    val songCoverUri: Uri
)