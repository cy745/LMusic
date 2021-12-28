package com.lalilu.lmusic.event

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.lalilu.lmusic.Config.LAST_PLAYLIST_ID
import com.lalilu.lmusic.adapter.node.FirstNode
import com.lalilu.lmusic.adapter.node.SecondNode
import com.lalilu.lmusic.database.LMusicDataBase
import com.lalilu.lmusic.domain.entity.MPlaylist
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
@ExperimentalCoroutinesApi
class DataModule @Inject constructor(
    database: LMusicDataBase
) : ViewModel(), CoroutineScope {
    private val mmkv = MMKV.defaultMMKV()
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    fun changePlaylistById(id: Long) = launch {
        _nowPlaylistId.emit(id)
    }

    private val _allPlaylist = database.playlistDao().getAllFlow()
    val allPlaylist: LiveData<List<FirstNode<MPlaylist>>> =
        _allPlaylist.mapLatest { playlists ->
            playlists.map { playlist ->
                FirstNode(playlist.songs.map { song ->
                    SecondNode(null, song)
                }, playlist.playlist)
            }
        }.asLiveData()

    private val _nowPlaylistId: MutableStateFlow<Long> =
        MutableStateFlow(mmkv.decodeLong(LAST_PLAYLIST_ID, 0L))

    private val _nowPlaylistMetadata: Flow<List<MediaMetadataCompat>> =
        _nowPlaylistId.flatMapLatest {
            database.playlistDao().getByIdFlow(it).mapLatest { playlist ->
                playlist?.songs?.map { song ->
                    song.toMediaMetadataCompat()
                } ?: ArrayList()
            }
        }

    val nowPlaylistMetadataFlow: Flow<List<MediaMetadataCompat>> = _nowPlaylistMetadata

    /**
     *  Metadata 转 MediaItem
     */
    private val nowPlaylistMediaItemFlow: Flow<List<MediaBrowserCompat.MediaItem>> =
        _nowPlaylistMetadata.mapLatest {
            it.map { metadata ->
                MediaBrowserCompat.MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setTitle(metadata.description.title)
                        .setMediaId(metadata.description.mediaId)
                        .setSubtitle(metadata.description.subtitle)
                        .setDescription(metadata.description.description)
                        .setIconUri(metadata.description.iconUri)
                        .setMediaUri(metadata.description.mediaUri)
                        .setExtras(metadata.bundle).build(),
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                )
            }
        }

    val nowPlaylistMediaItemLiveData: LiveData<List<MediaBrowserCompat.MediaItem>> =
        nowPlaylistMediaItemFlow.asLiveData()
}