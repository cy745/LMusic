package com.lalilu.lplaylist.repository

import android.app.Application
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.lplaylist.R
import com.lalilu.lplaylist.entity.LPlaylist
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
internal class PlaylistRepositoryImpl(
    private val kv: PlaylistKV,
    private val context: Application,
) : PlaylistRepository {

    override fun getPlaylistsFlow(): Flow<List<LPlaylist>> {
        return kv.playlistList.flow()
            .mapLatest { it ?: emptyList() }
    }

    override fun getPlaylists(): List<LPlaylist> {
        return kv.playlistList.value ?: emptyList()
    }

    override fun setPlaylists(playlists: List<LPlaylist>) {
        kv.playlistList.apply {
            value = playlists
            if (!autoSave) save()
        }
    }

    override fun save(playlist: LPlaylist) {
        val playlists = getPlaylists().toMutableList()
        val index = playlists.indexOfFirst { it.id == playlist.id }

        // 若已存在则更新
        if (index >= 0) {
            playlists[index] = playlist
            setPlaylists(playlists)
            return
        }

        playlists.add(0, playlist)
        setPlaylists(playlists)
    }

    override fun remove(playlist: LPlaylist) {
        val playlists = getPlaylists().toMutableList()
        playlists.remove(playlist)
        setPlaylists(playlists)
    }

    override fun removeById(id: String) {
        if (id == PlaylistRepository.FAVOURITE_PLAYLIST_ID) {
            ToastUtils.showShort(R.string.playlist_tips_cannot_remove_favourite)
            return
        }

        // 筛选不用删除的元素
        val result = getPlaylists().filter { it.id != id }
        setPlaylists(result)
    }

    override fun removeByIds(ids: List<String>) {
        if (ids.contains(PlaylistRepository.FAVOURITE_PLAYLIST_ID)) {
            ToastUtils.showShort(R.string.playlist_tips_cannot_remove_favourite)
        }

        // 筛选不用删除的元素
        val result = getPlaylists().filter {
            !ids.contains(it.id) || it.id == PlaylistRepository.FAVOURITE_PLAYLIST_ID
        }
        setPlaylists(result)
    }

    override fun isExist(playlistId: String): Boolean {
        return getPlaylists().any { it.id == playlistId }
    }

    override fun isExistInPlaylist(playlistId: String, mediaId: String): Boolean {
        val playlists = getPlaylists()
        val playlist = playlists.firstOrNull { it.id == playlistId } ?: return false
        return playlist.mediaIds.contains(mediaId)
    }

    override fun updateMediaIdsToPlaylist(mediaIds: List<String>, playlistId: String) {
        updatePlaylist(playlistId) { it.copy(mediaIds = mediaIds.distinct()) }
    }

    override fun addMediaIdsToPlaylist(mediaIds: List<String>, playlistId: String) {
        updatePlaylist(playlistId) { it.copy(mediaIds = it.mediaIds.plus(mediaIds).distinct()) }
    }

    override fun addMediaIdsToPlaylists(mediaIds: List<String>, playlistIds: List<String>) {
        var changed = false
        val playlists = getPlaylists().toMutableList()

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
        setPlaylists(playlists)
    }

    override fun removeMediaIdsFromPlaylist(mediaIds: List<String>, playlistId: String) {
        updatePlaylist(playlistId) { it.copy(mediaIds = it.mediaIds.minus(mediaIds.toSet())) }
    }

    override fun removeMediaIdsFromPlaylists(mediaIds: List<String>, playlistIds: List<String>) {
        var changed = false
        val playlists = getPlaylists().toMutableList()

        for (index in playlists.indices) {
            val playlist = playlists[index]
            val playlistId = playlist.id
            val exist = playlistIds.any { it == playlistId }
            if (!exist) continue

            changed = true
            val newMediaIds = playlist.mediaIds.minus(mediaIds.toSet())

            playlists[index] = playlist.copy(mediaIds = newMediaIds)
        }

        if (!changed) return
        setPlaylists(playlists)
    }

    override fun updateMediaIdsToFavourite(mediaIds: List<String>) {
        updateMediaIdsToPlaylist(mediaIds, PlaylistRepository.FAVOURITE_PLAYLIST_ID)
    }

    override fun addMediaIdsToFavourite(mediaIds: List<String>) {
        if (checkFavouriteExist()) {
            addMediaIdsToPlaylist(mediaIds, PlaylistRepository.FAVOURITE_PLAYLIST_ID)
        }
    }

    override fun removeMediaIdsFromFavourite(mediaIds: List<String>) {
        if (checkFavouriteExist()) {
            removeMediaIdsFromPlaylist(mediaIds, PlaylistRepository.FAVOURITE_PLAYLIST_ID)
        }
    }

    override fun checkFavouriteExist(): Boolean {
        val playlists = getPlaylists()
        val exist = playlists.any { it.id == PlaylistRepository.FAVOURITE_PLAYLIST_ID }

        if (!exist) {
            save(
                LPlaylist(
                    id = PlaylistRepository.FAVOURITE_PLAYLIST_ID,
                    title = context.getString(R.string.playlist_tips_favourite),
                    subTitle = context.getString(R.string.playlist_tips_favourite_subTitle),
                    coverUri = "",
                    mediaIds = emptyList()
                )
            )
        }

        return exist
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getFavouriteMediaIds(): Flow<List<String>> {
        return getPlaylistsFlow()
            .mapLatest { playlists ->
                playlists
                    .firstOrNull { it.id == PlaylistRepository.FAVOURITE_PLAYLIST_ID }
                    ?.mediaIds ?: emptyList()
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun isItemInFavourite(mediaId: String): Flow<Boolean> {
        return getFavouriteMediaIds()
            .mapLatest { it.contains(mediaId) }
    }

    private fun updatePlaylist(playlistId: String, action: (LPlaylist) -> LPlaylist) {
        val playlists = getPlaylists().toMutableList()
        val index = playlists.indexOfFirst { it.id == playlistId }.takeIf { it >= 0 } ?: return

        playlists[index] = action(playlists[index])
        setPlaylists(playlists)
    }
}