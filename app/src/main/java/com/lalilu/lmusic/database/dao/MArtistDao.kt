package com.lalilu.lmusic.database.dao

import androidx.room.*
import com.lalilu.lmusic.domain.entity.MArtist
import kotlinx.coroutines.flow.Flow

@Dao
interface MArtistDao {
    @Query("SELECT * FROM m_artist;")
    fun getAll(): Flow<List<MArtist>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(artist: MArtist)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(artist: List<MArtist>)

    @Query("DELETE FROM m_artist;")
    suspend fun deleteAll()

    @Delete
    fun delete(artist: MArtist)
}