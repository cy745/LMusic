package com.lalilu.lplaylist.repository

import com.lalilu.common.base.SpListItem
import com.lalilu.lplaylist.entity.LPlaylist


internal class PlaylistRepositoryImpl(
    private val sp: PlaylistSp
) : PlaylistRepository {
    override fun getPlaylists(): SpListItem<LPlaylist> {
        return sp.playlistList
    }

    override fun save(playlist: LPlaylist) {
        sp.playlistList.add(0, playlist)
    }

    override fun remove(playlist: LPlaylist) {
        sp.playlistList.remove(playlist)
    }

    override fun removeById(id: String) {
        val result = sp.playlistList.value.filter { it.id != id }
        sp.playlistList.value = result
    }

    override fun removeByIds(ids: List<String>) {
        val result = sp.playlistList.value.filter { !ids.contains(it.id) }
        sp.playlistList.value = result
    }

    override fun addMediaIdsToPlaylists(mediaIds: List<String>, playlistIds: List<String>) {
        var changed = false
        val playlists = sp.playlistList.value.toMutableList()

        for (index in playlists.indices) {
            val playlist = playlists[index]
            val playlistId = playlist.id
            val exist = playlistIds.any { it == playlistId }
            if (!exist) continue

            val mediaIdsSet = playlist.mediaIds.toHashSet()
                .also {
                    changed = true
                    it.addAll(mediaIds)
                }

            playlists[index] = playlist.copy(mediaIds = mediaIdsSet.toList())
        }

        if (!changed) return
        sp.playlistList.value = playlists
    }
}