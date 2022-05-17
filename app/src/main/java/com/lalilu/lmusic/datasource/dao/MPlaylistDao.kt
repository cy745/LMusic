package com.lalilu.lmusic.datasource.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.lalilu.lmusic.datasource.entity.MPlaylist

@Dao
interface MPlaylistDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(vararg playlist: MPlaylist)

    @Update(entity = MPlaylist::class)
    fun update(vararg playlist: MPlaylist)

    @Delete(entity = MPlaylist::class)
    fun delete(vararg playlist: MPlaylist)

    @Query("SELECT * FROM m_playlist;")
    fun getAll(): List<MPlaylist>

    @Query("SELECT * FROM m_playlist ORDER BY playlist_create_time DESC;")
    fun getAllLiveDataSortByTime(): LiveData<List<MPlaylist>>

    @Query("SELECT * FROM m_playlist WHERE playlist_id = :id;")
    fun getById(id: Long): MPlaylist?
}