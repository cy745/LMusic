package com.lalilu.media.dao

import androidx.room.*
import com.lalilu.media.entity.PlaylistMusicCrossRef
import com.lalilu.media.entity.PlaylistWithMusics

@Dao
interface PlayListDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCrossRef(pmCrossRef: PlaylistMusicCrossRef)

    @Transaction
    @Query("SELECT * FROM playlist WHERE playlistId = (:id)")
    fun getPlaylistWithSongsById(id: Long): PlaylistWithMusics

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlaylist(playlistWithMusics: PlaylistWithMusics) {
        val playlistId = playlistWithMusics.playlist.playlistId
        playlistWithMusics.musics.forEach {
            insertCrossRef(PlaylistMusicCrossRef(playlistId, it.musicId))
        }
    }
}