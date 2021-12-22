package com.lalilu.lmusic.event

import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.Config.LAST_PLAYLIST_ID
import com.lalilu.lmusic.database.LMusicDataBase
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataModule @Inject constructor(
    database: LMusicDataBase
) : ViewModel() {
    private val mmkv = MMKV.defaultMMKV()

    private val _nowPlaylistId: MutableStateFlow<Long> =
        MutableStateFlow(mmkv.decodeLong(LAST_PLAYLIST_ID, 0L))

    @ExperimentalCoroutinesApi
    val nowPlaylistId: Flow<Long>
        get() = _nowPlaylistId.mapLatest {
            mmkv.encode(LAST_PLAYLIST_ID, it)
            return@mapLatest it
        }


    @ExperimentalCoroutinesApi
    val nowPlaylist: Flow<List<MediaBrowserCompat.MediaItem>?> = nowPlaylistId.flatMapLatest {
        database.playlistDao().getByIdFlow(it).mapLatest { playlist ->
            playlist?.songs?.map { song ->
                MediaBrowserCompat.MediaItem(
                    song.toMediaMetadataCompat().description,
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                )
            }
        }
    }
}