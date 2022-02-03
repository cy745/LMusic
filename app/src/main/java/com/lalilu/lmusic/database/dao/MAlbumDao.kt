package com.lalilu.lmusic.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import androidx.room.Transaction
import com.lalilu.lmusic.domain.entity.AlbumWithSongs
import com.lalilu.lmusic.domain.entity.MAlbum

@Dao
interface MAlbumDao {

    @Transaction
    @Query("SELECT * FROM m_album")
    fun getAllAlbumWithSong(): List<AlbumWithSongs>

    @Query("SELECT * FROM m_album;")
    fun getAllAlbumLiveData(): LiveData<List<MAlbum>>

    @Transaction
    @Query("SELECT song_id FROM m_album AS a, m_song AS s WHERE a.album_id = s.album_id AND a.album_id = :id;")
    fun getSongsIdByAlbumId(id: Long): List<Long>

    @Insert(onConflict = IGNORE)
    fun save(album: MAlbum)

    @Query("DELETE FROM m_album WHERE album_id = :albumId;")
    fun deleteById(albumId: Long)
}
