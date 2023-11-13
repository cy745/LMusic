package com.lalilu.lplaylist.repository

import com.lalilu.lplaylist.entity.LPlaylist


internal class PlaylistRepositoryImpl(
    private val sp: PlaylistSp
) : PlaylistRepository {
    override fun save(playlist: LPlaylist) {
        sp.playlistList.add(0, playlist)
    }

    override fun remove(playlist: LPlaylist) {
        sp.playlistList.remove(playlist)
    }

    override fun removeById(id: String) {
        val list = sp.playlistList.value
        val index = list.indexOfFirst { it.id == id }

        if (index in list.indices) {
            val result = list.toMutableList()
            result.removeAt(index)
            sp.playlistList.value = result
        }
    }
}