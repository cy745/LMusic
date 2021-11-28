package com.lalilu.lmusic.database.dao

import androidx.room.*
import com.lalilu.lmusic.domain.entity.MPlaylist
import com.lalilu.lmusic.domain.entity.PlaylistWithSongs

@Dao
interface MPlaylistDao {
    @Transaction
    @Query("SELECT * FROM m_playlist WHERE playlist_id = :id;")
    fun getById(id: Long): PlaylistWithSongs

    @Transaction
    @Query("SELECT * FROM m_playlist;")
    fun getAll(): List<PlaylistWithSongs>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(playlist: MPlaylist)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(playlist: List<MPlaylist>)

    @Query("DELETE FROM m_playlist;")
    suspend fun deleteAll()
}