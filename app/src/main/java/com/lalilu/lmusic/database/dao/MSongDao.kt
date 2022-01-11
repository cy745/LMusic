package com.lalilu.lmusic.database.dao

import androidx.room.*
import com.lalilu.lmusic.domain.entity.FullSongInfo
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.domain.entity.MSongUpdateCoverUri

@Dao
interface MSongDao {
    @Query("SELECT * FROM m_song WHERE song_id = :songId")
    fun getById(songId: Long): MSong?

    @Query("SELECT * FROM m_song")
    fun getAll(): List<MSong>

    @Transaction
    @Query("SELECT * FROM m_song")
    fun getAllFullSong(): List<FullSongInfo>

    @Transaction
    @Query("SELECT * FROM m_song WHERE song_id = :songId")
    fun getSingleFullSong(songId: Long): FullSongInfo

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(song: MSong)

    @Query("DELETE FROM m_song;")
    suspend fun deleteAll()

    @Update(entity = MSong::class)
    fun updateCoverUri(update: MSongUpdateCoverUri)

    @Delete
    fun delete(song: MSong)
}