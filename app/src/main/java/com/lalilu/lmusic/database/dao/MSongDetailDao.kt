package com.lalilu.lmusic.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lalilu.lmusic.domain.entity.MSongDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface MSongDetailDao {
    @Query("SELECT * FROM m_song_detail")
    fun getAll(): List<MSongDetail>

    @Query("SELECT * FROM m_song_detail WHERE song_id = :songId")
    fun getById(songId: Long): MSongDetail?

    @Query("SELECT * FROM m_song_detail WHERE song_id = :songId")
    fun getByIdStr(songId: String): MSongDetail?

    @Query("SELECT * FROM m_song_detail WHERE song_id = :songId")
    fun getByIdStrFlow(songId: String): Flow<MSongDetail?>

    @Insert
    fun save(songDetail: MSongDetail)

    @Query("DELETE FROM m_song_detail;")
    suspend fun deleteAll()
}