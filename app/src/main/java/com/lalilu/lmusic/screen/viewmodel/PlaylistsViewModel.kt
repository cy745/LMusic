package com.lalilu.lmusic.screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.datasource.ITEM_PREFIX
import com.lalilu.lmusic.datasource.LMusicDataBase
import com.lalilu.lmusic.datasource.SongInPlaylist
import com.lalilu.lmusic.datasource.entity.MPlaylist
import com.lalilu.lmusic.service.MSongBrowser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val dataBase: LMusicDataBase,
    private val mediaBrowser: MSongBrowser,
    private val mediaSource: BaseMediaSource
) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    val playlists = dataBase.playlistDao().getAllLiveDataSortByTime()

    suspend fun getSongsByPlaylistId(playlistId: Long): List<MediaItem> =
        withContext(Dispatchers.IO) {
            return@withContext dataBase.songInPlaylistDao()
                .getAllByPlaylistId(playlistId)
                .mapNotNull {
                    mediaSource.getItemById(ITEM_PREFIX + it.mediaId)
                }
        }

    suspend fun getPlaylistById(playlistId: Long): MPlaylist? =
        withContext(Dispatchers.IO) {
            return@withContext dataBase.playlistDao().getById(playlistId)
        }

    fun createNewPlaylist(title: String? = null) = launch {
        dataBase.playlistDao().save(
            MPlaylist(playlistTitle = title ?: "空歌单")
        )
    }

    fun removePlaylist(playlist: MPlaylist) = launch {
        dataBase.playlistDao().delete(playlist)
    }

    fun copyCurrentPlayingPlaylist() = launch {
        val playlistTitle = "复制歌单: (${mediaBrowser.originPlaylistIds.size})"
        val playlist = MPlaylist(playlistTitle = playlistTitle)
        dataBase.playlistDao().save(playlist)
        dataBase.songInPlaylistDao().save(
            mediaBrowser.originPlaylistIds.map {
                SongInPlaylist(
                    playlistId = playlist.playlistId,
                    mediaId = it
                )
            }
        )
    }

    fun addSongsIntoPlaylists(mediaIds: List<String>, playlistIds: List<Long>) = launch {
        val songInPlaylist = playlistIds.flatMap { playlistId ->
            mediaIds.map { mediaId ->
                SongInPlaylist(playlistId = playlistId, mediaId = mediaId)
            }
        }
        dataBase.songInPlaylistDao().save(songInPlaylist)
    }
}