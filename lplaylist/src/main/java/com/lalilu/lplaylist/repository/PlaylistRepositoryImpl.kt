package com.lalilu.lplaylist.repository

import com.blankj.utilcode.util.ToastUtils
import com.lalilu.common.base.SpListItem
import com.lalilu.lplaylist.R
import com.lalilu.lplaylist.entity.LPlaylist
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest


internal class PlaylistRepositoryImpl(
    private val sp: PlaylistSp
) : PlaylistRepository {
    override fun getPlaylists(): SpListItem<LPlaylist> {
        return sp.playlistList
    }

    override fun save(playlist: LPlaylist) {
        sp.playlistList.add(0, playlist)
        sp.playlistList.save()
    }

    override fun remove(playlist: LPlaylist) {
        sp.playlistList.remove(playlist)
        sp.playlistList.save()
    }

    override fun removeById(id: String) {
        if (id == PlaylistRepository.FAVOURITE_PLAYLIST_ID) {
            ToastUtils.showShort(R.string.playlist_tips_cannot_remove_favourite)
            return
        }

        val result = sp.playlistList.value.filter { it.id != id }
        sp.playlistList.value = result
        sp.playlistList.save()
    }

    override fun removeByIds(ids: List<String>) {
        if (ids.contains(PlaylistRepository.FAVOURITE_PLAYLIST_ID)) {
            ToastUtils.showShort(R.string.playlist_tips_cannot_remove_favourite)
        }

        val result = sp.playlistList.value.filter {
            !ids.contains(it.id) || it.id == PlaylistRepository.FAVOURITE_PLAYLIST_ID
        }
        sp.playlistList.value = result
        sp.playlistList.save()
    }

    override fun isExist(playlistId: String): Boolean {
        return sp.playlistList.value.any { it.id == playlistId }
    }

    override fun isExistInPlaylist(playlistId: String, mediaId: String): Boolean {
        val playlists = sp.playlistList.value.toMutableList()
        val playlist = playlists.firstOrNull { it.id == playlistId } ?: return false
        return playlist.mediaIds.contains(mediaId)
    }

    override fun addMediaIdsToPlaylist(mediaIds: List<String>, playlistId: String) {
        val playlists = sp.playlistList.value.toMutableList()
        val index = playlists.indexOfFirst { it.id == playlistId }.takeIf { it >= 0 } ?: return
        val playlist = playlists[index]

        playlists[index] = playlist.copy(mediaIds = playlist.mediaIds.plus(mediaIds.toSet()))
        sp.playlistList.value = playlists
        sp.playlistList.save()
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
        sp.playlistList.save()
    }

    override fun removeMediaIdsFromPlaylist(mediaIds: List<String>, playlistId: String) {
        val playlists = sp.playlistList.value.toMutableList()
        val index = playlists.indexOfFirst { it.id == playlistId }.takeIf { it >= 0 } ?: return
        val playlist = playlists[index]

        playlists[index] = playlist.copy(mediaIds = playlist.mediaIds.minus(mediaIds.toSet()))
        sp.playlistList.value = playlists
        sp.playlistList.save()
    }

    override fun removeMediaIdsFromPlaylists(mediaIds: List<String>, playlistIds: List<String>) {
        var changed = false
        val playlists = sp.playlistList.value.toMutableList()

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
        sp.playlistList.value = playlists
        sp.playlistList.save()
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
        val playlists = sp.playlistList.value
        val exist = playlists.any { it.id == PlaylistRepository.FAVOURITE_PLAYLIST_ID }

        if (!exist) {
            save(
                LPlaylist(
                    id = PlaylistRepository.FAVOURITE_PLAYLIST_ID,
                    title = "",
                    subTitle = "",
                    coverUri = "",
                    mediaIds = emptyList()
                )
            )
        }

        return exist
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getFavouriteMediaIds(): Flow<List<String>> {
        return sp.playlistList.flow(true)
            .mapLatest { playlists ->
                playlists
                    ?.firstOrNull { it.id == PlaylistRepository.FAVOURITE_PLAYLIST_ID }
                    ?.mediaIds ?: emptyList()
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun isItemInFavourite(mediaId: String): Flow<Boolean> {
        return getFavouriteMediaIds()
            .mapLatest { it.contains(mediaId) }
    }
}