package com.lalilu.lplaylist.repository

import com.lalilu.common.base.SpListItem
import com.lalilu.lplaylist.entity.LPlaylist

interface PlaylistRepository {
    fun getPlaylists(): SpListItem<LPlaylist>
    fun save(playlist: LPlaylist)
    fun remove(playlist: LPlaylist)
    fun removeById(id: String)
    fun removeByIds(ids: List<String>)

    fun addMediaIdsToPlaylists(mediaIds: List<String>, playlistIds: List<String>)
    fun removeMediaIdsFromPlaylist(mediaIds: List<String>, playlistId: String)
}