package com.lalilu.lmusic.database.dao

import androidx.room.*
import com.lalilu.lmusic.domain.entity.MAlbum

@Dao
interface MAlbumDao {

    @Transaction
    @Query("SELECT song_id FROM m_album AS a, m_song AS s WHERE a.album_id = s.album_id AND a.album_id = :id;")
    fun getSongsIdByAlbumId(id: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(album: MAlbum)

    @Query("DELETE FROM m_album;")
    suspend fun deleteAll()
}