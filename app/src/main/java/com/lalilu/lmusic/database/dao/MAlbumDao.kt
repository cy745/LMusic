package com.lalilu.lmusic.database.dao

import androidx.room.*
import com.lalilu.lmusic.domain.entity.AlbumWithSongs
import com.lalilu.lmusic.domain.entity.MAlbum

@Dao
interface MAlbumDao {
    @Query("SELECT * FROM m_album;")
    fun getAll(): List<MAlbum>

    @Transaction
    @Query("SELECT * FROM m_album;")
    fun getAllFullAlbum(): List<AlbumWithSongs>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(album: MAlbum)

    @Query("DELETE FROM m_album;")
    suspend fun deleteAll()
}