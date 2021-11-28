package com.lalilu.lmusic.database.dao

import androidx.room.*
import com.lalilu.lmusic.domain.entity.*

@Dao
interface MSongDao {
    @Query("SELECT * FROM m_song WHERE song_id = :songId")
    fun getById(songId: Long): MSong

    @Query("SELECT * FROM m_song")
    fun getAll(): List<MSong>

    @Transaction
    @Query("SELECT * FROM m_song")
    fun getAllFullSong(): List<FullSongInfo>

    @Transaction
    @Query("SELECT * FROM m_song WHERE song_id = :songId")
    fun getSingleFullSong(songId: Long): FullSongInfo

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveSongWithArtists(song: MSong, artist: MArtist, crossRef: ArtistSongCrossRef)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveSongWithArtists(song: MSong, artists: List<MArtist>) {
        artists.forEach { artist ->
            saveSongWithArtists(
                song, artist, ArtistSongCrossRef(song.songId, artist.artistName)
            )
        }
    }

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveSongWithPlaylist(song: MSong, playlists: List<MPlaylist>)

    @Query("DELETE FROM m_song;")
    suspend fun deleteAll()

    @Delete
    fun delete(song: MSong)

    // 60219
    // QU4RTZ[中須かすみ(CV.相良茉優)、近江彼方(CV.鬼頭明里)、エマ・ヴェルデ(CV.指出毬亜)、天王寺璃奈(CV.田中ちえ美)]

    // 60225
    // DiverDiva
}