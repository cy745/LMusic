package com.lalilu.lmusic.database.repository

import com.lalilu.lmusic.database.dao.MAlbumDao
import com.lalilu.lmusic.database.dao.MPlaylistDao
import javax.inject.Inject

class ListRepository @Inject constructor(
    val playlistDao: MPlaylistDao,
    val albumDao: MAlbumDao
)