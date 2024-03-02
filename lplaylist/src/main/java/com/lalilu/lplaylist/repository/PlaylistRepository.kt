package com.lalilu.lplaylist.repository

import com.lalilu.lplaylist.entity.LPlaylist
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    companion object {
        const val FAVOURITE_PLAYLIST_ID = "FAVOURITE"
    }

    fun getPlaylistsFlow(): Flow<List<LPlaylist>>
    fun getPlaylists(): List<LPlaylist>
    fun setPlaylists(playlists: List<LPlaylist>)

    fun save(playlist: LPlaylist)
    fun remove(playlist: LPlaylist)
    fun removeById(id: String)
    fun removeByIds(ids: List<String>)
    fun isExist(playlistId: String): Boolean
    fun isExistInPlaylist(playlistId: String, mediaId: String): Boolean

    fun updateMediaIdsToPlaylist(mediaIds: List<String>, playlistId: String)
    fun addMediaIdsToPlaylist(mediaIds: List<String>, playlistId: String)
    fun addMediaIdsToPlaylists(mediaIds: List<String>, playlistIds: List<String>)
    fun removeMediaIdsFromPlaylist(mediaIds: List<String>, playlistId: String)
    fun removeMediaIdsFromPlaylists(mediaIds: List<String>, playlistIds: List<String>)

    fun updateMediaIdsToFavourite(mediaIds: List<String>)
    fun addMediaIdsToFavourite(mediaIds: List<String>)
    fun removeMediaIdsFromFavourite(mediaIds: List<String>)

    /**
     * 检查我喜欢歌单是否存在，若不存在则创建
     */
    fun checkFavouriteExist(): Boolean
    fun getFavouriteMediaIds(): Flow<List<String>>
    fun isItemInFavourite(mediaId: String): Flow<Boolean>
}