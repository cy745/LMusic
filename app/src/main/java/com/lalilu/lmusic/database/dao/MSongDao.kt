package com.lalilu.lmusic.database.dao

import androidx.room.*
import com.lalilu.lmusic.domain.entity.FullSongInfo
import com.lalilu.lmusic.domain.entity.MSong
import kotlinx.coroutines.flow.Flow

@Dao
interface MSongDao {
    @Query("SELECT * FROM m_song WHERE song_id = :songId")
    fun getById(songId: Long): MSong?

    @Transaction
    @Query("SELECT * FROM m_song")
    fun getAllFullSongFlow(): Flow<List<FullSongInfo>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(song: MSong)

    @Query("DELETE FROM m_song;")
    suspend fun deleteAll()

    @Delete
    fun delete(song: MSong)
}