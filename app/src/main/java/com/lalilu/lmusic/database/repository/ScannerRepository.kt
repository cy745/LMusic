package com.lalilu.lmusic.database.repository

import com.lalilu.lmusic.database.dao.MAlbumDao
import com.lalilu.lmusic.database.dao.MRelationDao
import com.lalilu.lmusic.database.dao.MSongDetailDao
import com.lalilu.lmusic.domain.entity.*
import javax.inject.Inject

class ScannerRepository @Inject constructor(
    val relationDao: MRelationDao,
    val songDetailDao: MSongDetailDao,
    val albumDao: MAlbumDao
) {
    fun saveAlbum(album: MAlbum) = albumDao.save(album)
    fun saveSongDetail(songDetail: MSongDetail) = songDetailDao.save(songDetail)
    fun saveSongXArtist(song: MSong, artists: List<MArtist>) =
        relationDao.saveSongXArtist(song, artists)

    fun savePlaylistXSong(playlist: MPlaylist, song: MSong) =
        relationDao.savePlaylistXSong(playlist, song)
}