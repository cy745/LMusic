package com.lalilu.lplaylist.repository

import com.lalilu.lplaylist.entity.LPlaylist

interface PlaylistRepository {

    fun save(playlist: LPlaylist)
    fun remove(playlist: LPlaylist)
    fun removeById(id: String)
}