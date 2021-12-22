package com.lalilu.lmusic.database.dao

import androidx.room.*
import com.lalilu.lmusic.domain.entity.MPlaylist
import com.lalilu.lmusic.domain.entity.PlaylistWithSongs
import kotlinx.coroutines.flow.Flow

@Dao
interface MPlaylistDao {
    @Transaction
    @Query("SELECT * FROM m_playlist WHERE playlist_id = :id;")
    fun getById(id: Long): PlaylistWithSongs?

    @Transaction
    @Query("SELECT * FROM m_playlist WHERE playlist_id = :id;")
    fun getByIdFlow(id: Long): Flow<PlaylistWithSongs?>

    @Transaction
    @Query("SELECT * FROM m_playlist;")
    fun getAll(): List<PlaylistWithSongs>

    @Transaction
    @Query("SELECT * FROM m_playlist;")
    fun getAllFlow(): Flow<List<PlaylistWithSongs>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(playlist: MPlaylist)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(playlist: List<MPlaylist>)

    @Query("DELETE FROM m_playlist;")
    suspend fun deleteAll()
}