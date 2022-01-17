package com.lalilu.lmusic.database.dao

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.room.*
import com.lalilu.lmusic.domain.entity.MPlaylist
import kotlinx.coroutines.flow.Flow

@Dao
interface MPlaylistDao {
    @Query("SELECT * FROM m_playlist;")
    fun getAllPlaylistLiveData(): LiveData<List<MPlaylist>>

    @Query("SELECT * FROM m_playlist;")
    fun getAllPlaylistFlow(): Flow<List<MPlaylist>>

    @Query("SELECT s.song_cover_uri FROM playlist_song_cross_ref AS c,m_song_detail AS s WHERE c.song_id = s.song_id AND c.playlist_id = :id;")
    fun getLastCreateSongCoverByPlaylistId(id: Long): Uri

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