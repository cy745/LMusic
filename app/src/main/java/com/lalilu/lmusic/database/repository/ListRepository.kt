package com.lalilu.lmusic.database.repository

import com.lalilu.lmusic.database.dao.MAlbumDao
import com.lalilu.lmusic.database.dao.MPlaylistDao
import com.lalilu.lmusic.database.dao.MSongDao
import javax.inject.Inject

class ListRepository @Inject constructor(
    val playlistDao: MPlaylistDao,
    val albumDao: MAlbumDao,
    val songDao: MSongDao
)