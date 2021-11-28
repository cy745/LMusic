package com.lalilu.lmusic.database.dao

import androidx.room.*
import com.lalilu.lmusic.domain.entity.MPlaylist
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.domain.entity.PlaylistSongCrossRef
import com.lalilu.lmusic.domain.entity.PlaylistWithSongs

@Dao
interface MPlaylistDao {
    @Transaction
    @Query("SELECT * FROM m_playlist;")
    fun getAll(): List<PlaylistWithSongs>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(playlist: MPlaylist)


    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePlaylistWithSongs(playlist: MPlaylist, song: MSong, crossRef: PlaylistSongCrossRef)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePlaylistWithSongs(playlist: MPlaylist, songs: List<MSong>) {
        songs.forEach { song ->
            savePlaylistWithSongs(
                playlist, song,
                PlaylistSongCrossRef(playlist.playlistId, song.songId)
            )
        }
    }

    @Query("DELETE FROM m_playlist;")
    suspend fun deleteAll()
}