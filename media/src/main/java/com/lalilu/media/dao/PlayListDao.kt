package com.lalilu.media.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.lalilu.media.entity.Music
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
    fun getPlaylistWithSongsById(id: Long): PlaylistWithMusics?

    @Query("SELECT * FROM playlist")
    fun getAllLiveData(): LiveData<List<Playlist>>

    @Query("SELECT * FROM playlist")
    fun getAll(): List<Playlist>

    fun getPlaylistWithSongs(): List<PlaylistWithMusics> {
        return getAll().map {
            getPlaylistWithSongsByIdWithOrder(it.playlistId)!!
        }
    }

    fun getPlaylistWithSongsByIdWithOrder(id: Long): PlaylistWithMusics? {
        val playlist = getPlaylistWithSongsById(id) ?: return null
        val orderList = playlist.playlist?.playlistOrder ?: return playlist
        val musicList = playlist.musics
        playlist.musics = changeOrderByOrderList(musicList, orderList)
        return playlist
    }

    private fun changeOrderByOrderList(
        list: List<Music>?,
        orderList: List<Long>
    ): ArrayList<Music> {
        val musics = ArrayList<Music>()
        for (id in orderList) {
            val music = list?.find { music -> music.musicId == id } ?: continue
            musics.add(music)
        }
        return musics
    }

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
    fun insertPlaylistByList(playlist: Playlist, musicId: ArrayList<Long>) {
        playlist.playlistOrder = musicId
        val playlistId = insertPlayList(playlist)
        musicId.forEach {
            insertCrossRef(PlaylistMusicCrossRef(playlistId, it))
        }
    }
}