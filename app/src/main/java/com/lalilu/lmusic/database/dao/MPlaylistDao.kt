package com.lalilu.lmusic.database.dao

import androidx.room.*
import com.lalilu.lmusic.domain.entity.MPlaylist

@Dao
interface MPlaylistDao {

    @Transaction
    @Query("SELECT song_id FROM playlist_song_cross_ref WHERE playlist_id = :id;")
    fun getSongsIdByPlaylistId(id: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(playlist: MPlaylist)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(playlist: List<MPlaylist>)

    @Query("DELETE FROM m_playlist;")
    suspend fun deleteAll()
}