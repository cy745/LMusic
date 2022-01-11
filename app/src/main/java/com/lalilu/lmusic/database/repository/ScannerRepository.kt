package com.lalilu.lmusic.database.repository

import android.net.Uri
import com.lalilu.lmusic.database.dao.MAlbumDao
import com.lalilu.lmusic.database.dao.MRelationDao
import com.lalilu.lmusic.database.dao.MSongDao
import com.lalilu.lmusic.database.dao.MSongDetailDao
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
        songDao.updateCoverUri(MSongUpdateCoverUri(songId, songCoverUri))
}