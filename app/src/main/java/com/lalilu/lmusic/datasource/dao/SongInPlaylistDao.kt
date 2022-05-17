package com.lalilu.lmusic.datasource.dao

import androidx.room.*
import com.lalilu.lmusic.datasource.entity.SongInPlaylist

@Dao
interface SongInPlaylistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(vararg songInPlaylist: SongInPlaylist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(songInPlaylists: List<SongInPlaylist>)

    @Delete(entity = SongInPlaylist::class)
    fun delete(vararg songInPlaylist: SongInPlaylist)

    @Query("SELECT * FROM song_in_playlist WHERE song_in_playlist_playlist_id = :playlistId;")
    fun getAllByPlaylistId(playlistId: Long): List<SongInPlaylist>
}