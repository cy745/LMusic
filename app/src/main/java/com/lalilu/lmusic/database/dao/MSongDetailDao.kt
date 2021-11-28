package com.lalilu.lmusic.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lalilu.lmusic.domain.entity.MSongDetail

@Dao
interface MSongDetailDao {
    @Query("SELECT * FROM m_song_detail")
    fun getAll(): List<MSongDetail>

    @Insert
    fun save(songDetail: MSongDetail)

    @Query("DELETE FROM m_song_detail;")
    suspend fun deleteAll()
}