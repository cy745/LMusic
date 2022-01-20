package com.lalilu.lmusic.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.lalilu.lmusic.domain.entity.*

@Dao
interface MRelationDao {
    @Insert(onConflict = IGNORE)
    fun savePlaylist(playlist: MPlaylist)

    @Insert(onConflict = REPLACE)
    fun saveSong(song: MSong)

    @Insert(onConflict = REPLACE)
    fun saveSongs(songs: List<MSong>)

    @Insert(onConflict = IGNORE)
    fun saveArtist(artist: MArtist)

    @Insert(onConflict = IGNORE)
    fun saveArtists(artist: List<MArtist>)

    @Insert(onConflict = IGNORE)
    fun saveCrossRef(crossRef: PlaylistSongCrossRef)

    @Insert(onConflict = IGNORE)
    fun saveCrossRef(crossRef: ArtistSongCrossRef)

    @Transaction
    fun savePlaylistXSongs(playlist: MPlaylist, songs: List<MSong>) {
        savePlaylist(playlist)
        saveSongs(songs)
        songs.forEach {
            saveCrossRef(PlaylistSongCrossRef(it.songId, playlist.playlistId))
        }
    }

    @Transaction
    fun savePlaylistXSong(playlist: MPlaylist, song: MSong) {
        savePlaylist(playlist)
        saveSong(song)
        saveCrossRef(PlaylistSongCrossRef(song.songId, playlist.playlistId))
    }

    @Transaction
    fun saveSongXArtist(song: MSong, artists: List<MArtist>) {
        saveSong(song)
        saveArtists(artists)
        artists.forEach {
            saveCrossRef(ArtistSongCrossRef(song.songId, it.artistName))
        }
    }

    @Query("DELETE FROM playlist_song_cross_ref WHERE song_id = :songId;")
    fun deleteBySongId(songId: Long)
}