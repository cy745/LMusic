package com.lalilu.lplaylist

interface PlaylistRepository {

    fun save(playlist: LPlaylist)
    fun remove(playlist: LPlaylist)
}