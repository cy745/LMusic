package com.lalilu.media.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.lalilu.media.entity.Playlist
import com.lalilu.media.entity.PlaylistMusicCrossRef
import com.lalilu.media.entity.PlaylistWithMusics

@Dao
interface PlayListDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayList(playlist: Playlist): Long

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCrossRef(pmCrossRef: PlaylistMusicCrossRef)

    @Transaction
    @Query("SELECT * FROM playlist WHERE playlistId = (:id)")
    fun getPlaylistWithSongsById(id: Long): PlaylistWithMusics

    @Transaction
    @Query("SELECT * FROM playlist")
    fun getPlaylistWithSongs(): List<PlaylistWithMusics>

    @Query("SELECT * FROM playlist")
    fun getAllLiveData(): LiveData<List<Playlist>>

    @Query("SELECT * FROM playlist")
    fun getAll(): List<Playlist>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlaylist(playlistWithMusics: PlaylistWithMusics) {
        val playlistId = playlistWithMusics.playlist?.playlistId ?: return
        playlistWithMusics.musics?.forEach {
            insertCrossRef(PlaylistMusicCrossRef(playlistId, it.musicId))
        }
    }

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlaylistByList(playlist: Playlist, musicId: List<String>?) {
        val playlistId = insertPlayList(playlist)
        musicId?.forEach {
            insertCrossRef(PlaylistMusicCrossRef(playlistId, it.toLong()))
        }
    }
}