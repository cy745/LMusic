package com.lalilu.lmusic.datasource.dao

import androidx.room.*
import com.lalilu.lmusic.datasource.entity.PlayHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayHistoryDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(vararg history: PlayHistory)

    @Update(entity = PlayHistory::class)
    fun update(vararg history: PlayHistory)

    @Delete(entity = PlayHistory::class)
    fun delete(vararg history: PlayHistory)

    @Query("SELECT * FROM m_play_history;")
    fun getAll(): List<PlayHistory>

    @Query("SELECT * FROM m_play_history WHERE id = :id;")
    fun getById(id: Long): PlayHistory?

    @Query("SELECT * FROM m_play_history ORDER BY startTime DESC LIMIT :count;")
    fun getFlow(count: Long): Flow<List<PlayHistory>>
}