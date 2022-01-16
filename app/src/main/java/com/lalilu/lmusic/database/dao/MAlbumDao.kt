package com.lalilu.lmusic.database.dao

import androidx.room.*
import com.lalilu.lmusic.domain.entity.AlbumWithSongs
import com.lalilu.lmusic.domain.entity.FullSongInfo
import com.lalilu.lmusic.domain.entity.MAlbum
import kotlinx.coroutines.flow.Flow

@Dao
interface MAlbumDao {
    @Query("SELECT * FROM m_album;")
    fun getAll(): List<MAlbum>

    @Transaction
    @Query("SELECT * FROM m_album;")
    fun getAllFullAlbum(): List<AlbumWithSongs>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM m_album AS a, m_song WHERE a.album_id = :id;")
    fun getFullSongInfoListByIdFlow(id: Long): Flow<List<FullSongInfo>?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(album: MAlbum)

    @Query("DELETE FROM m_album;")
    suspend fun deleteAll()
}