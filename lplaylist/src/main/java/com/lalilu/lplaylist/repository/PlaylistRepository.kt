package com.lalilu.lplaylist.repository

import com.lalilu.common.base.SpListItem
import com.lalilu.lplaylist.entity.LPlaylist

interface PlaylistRepository {
    companion object {
        const val FAVOURITE_PLAYLIST_ID = "FAVOURITE"
    }

    fun getPlaylists(): SpListItem<LPlaylist>
    fun save(playlist: LPlaylist)
    fun remove(playlist: LPlaylist)
    fun removeById(id: String)
    fun removeByIds(ids: List<String>)

    fun addMediaIdsToPlaylist(mediaIds: List<String>, playlistId: String)
    fun addMediaIdsToPlaylists(mediaIds: List<String>, playlistIds: List<String>)
    fun removeMediaIdsFromPlaylist(mediaIds: List<String>, playlistId: String)
    fun removeMediaIdsFromPlaylists(mediaIds: List<String>, playlistIds: List<String>)

    fun addMediaIdsToFavourite(mediaIds: List<String>)
    fun removeMediaIdsFromFavourite(mediaIds: List<String>)

    /**
     * 检查我喜欢歌单是否存在，若不存在则创建
     */
    fun checkFavouriteExist(): Boolean
}