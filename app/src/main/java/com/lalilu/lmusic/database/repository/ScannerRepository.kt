package com.lalilu.lmusic.database.repository

import android.net.Uri
import com.lalilu.lmusic.database.dao.*
import com.lalilu.lmusic.domain.entity.*
import javax.inject.Inject

class ScannerRepository @Inject constructor(
    val relationDao: MRelationDao,
    val songDetailDao: MSongDetailDao,
    val albumDao: MAlbumDao,
    val songDao: MSongDao,
) {
    fun saveAlbum(album: MAlbum) = albumDao.save(album)
    fun saveSongDetail(songDetail: MSongDetail) = songDetailDao.save(songDetail)
    fun saveSongXArtist(songId: Long, artists: List<MArtist>) {
        relationDao.saveArtists(artists)
        artists.forEach {
            relationDao.saveCrossRef(ArtistSongCrossRef(songId, it.artistName))
        }
    }

    fun savePlaylistXSong(playlist: MPlaylist, song: MSong) =
        relationDao.savePlaylistXSong(playlist, song)

    fun updateSongCoverUri(songId: Long, songCoverUri: Uri) =
        songDetailDao.updateCoverUri(MSongDetailUpdateCoverUri(songId, songCoverUri))

    fun updateAlbumCoverUri(albumId: Long, songCoverUri: Uri) =
        albumDao.updateCoverUri(MSongAlbumUpdateCoverUri(albumId, songCoverUri))

    fun updateSongLyric(songId: Long, lyric: String, songSize: Long, songData: String) =
        songDetailDao.updateLyric(MSongDetailUpdateLyric(songId, lyric, songSize, songData))
}