package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmusic.datasource.MDataBase
import com.lalilu.lmusic.datasource.entity.MPlaylist
import com.lalilu.lmusic.datasource.entity.SongInPlaylist
import com.lalilu.lmusic.manager.HistoryManager
import com.lalilu.lmusic.utils.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
//    private val mediaSource: MMediaSource,
    private val dataBase: MDataBase
) : ViewModel() {
    val playlists = dataBase.playlistDao().getAllLiveDataSortByTime()

//    suspend fun getSongsByPlaylistId(playlistId: Long): List<MediaItem> =
//        withContext(Dispatchers.IO) {
//            return@withContext dataBase.songInPlaylistDao()
//                .getAllByPlaylistId(playlistId)
//                .mapNotNull {
//                    mediaSource.getItemById(ITEM_PREFIX + it.mediaId)
//                }
//        }

    suspend fun getPlaylistById(playlistId: Long): MPlaylist? =
        withContext(Dispatchers.IO) {
            return@withContext dataBase.playlistDao().getById(playlistId)
        }

    fun createNewPlaylist(title: String? = null) = viewModelScope.safeLaunch(Dispatchers.IO) {
        dataBase.playlistDao().save(
            MPlaylist(playlistTitle = title ?: "空歌单")
        )
    }

    fun removePlaylist(playlist: MPlaylist) = viewModelScope.safeLaunch(Dispatchers.IO) {
        dataBase.playlistDao().delete(playlist)
    }

    fun copyCurrentPlayingPlaylist() = viewModelScope.safeLaunch(Dispatchers.IO) {
        val playlistTitle = "复制歌单: (${HistoryManager.currentPlayingIds.size})"
        val playlist = MPlaylist(playlistTitle = playlistTitle)
        dataBase.playlistDao().save(playlist)
        dataBase.songInPlaylistDao().save(
            HistoryManager.currentPlayingIds.map {
                SongInPlaylist(
                    playlistId = playlist.playlistId,
                    mediaId = it
                )
            }
        )
    }

    fun addSongsIntoPlaylists(mediaIds: List<String>, playlistIds: List<Long>) =
        viewModelScope.safeLaunch(Dispatchers.IO) {
            val songInPlaylist = playlistIds.flatMap { playlistId ->
                mediaIds.map { mediaId ->
                    SongInPlaylist(playlistId = playlistId, mediaId = mediaId)
                }
            }
            dataBase.songInPlaylistDao().save(songInPlaylist)
        }
}