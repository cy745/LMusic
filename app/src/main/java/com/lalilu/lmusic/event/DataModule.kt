package com.lalilu.lmusic.event

import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dirror.lyricviewx.LyricEntry
import com.dirror.lyricviewx.LyricUtil
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.Config.LAST_PLAYLIST_ID
import com.lalilu.lmusic.Config.LAST_REPEAT_MODE
import com.lalilu.lmusic.adapter.node.FirstNode
import com.lalilu.lmusic.adapter.node.SecondNode
import com.lalilu.lmusic.database.LMusicDataBase
import com.lalilu.lmusic.domain.entity.MPlaylist
import com.lalilu.lmusic.domain.entity.MSongDetail
import com.lalilu.lmusic.domain.entity.PlaylistWithSongs
import com.lalilu.lmusic.manager.LMusicNotificationManager
import com.lalilu.lmusic.utils.Mathf
import com.lalilu.lmusic.utils.getLastMediaMetadata
import com.lalilu.lmusic.utils.getLastPlaybackState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
@ExperimentalCoroutinesApi
class DataModule @Inject constructor(
    @ApplicationContext context: Context,
    database: LMusicDataBase,
    notificationManager: LMusicNotificationManager,
) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val sharedPref = context.getSharedPreferences(
        Config.SHARED_PLAYER, Context.MODE_PRIVATE
    )

    fun changePlaylistById(id: Long) = launch {
        _nowPlaylistId.emit(id)
    }

    fun updatePlaybackState(playbackStateCompat: PlaybackStateCompat?) = launch {
        _playBackState.emit(playbackStateCompat)
    }

    fun updateMetadata(metadataCompat: MediaMetadataCompat?) = launch {
        _metadata.emit(metadataCompat)
    }

    fun updateRepeatMode(repeatMode: Int) = launch {
        _repeatMode.emit(repeatMode)
        with(sharedPref.edit()) {
            this.putInt(LAST_REPEAT_MODE, repeatMode)
            this.apply()
        }
    }

    /***************************************/
    /**        PIN： 获取基础数据          **/
    /***************************************/
    private val _playBackState: MutableStateFlow<PlaybackStateCompat?> =
        MutableStateFlow(sharedPref.getLastPlaybackState())
    private val _metadata: MutableStateFlow<MediaMetadataCompat?> =
        MutableStateFlow(sharedPref.getLastMediaMetadata())
    private val _repeatMode: MutableStateFlow<Int> =
        MutableStateFlow(sharedPref.getInt(LAST_REPEAT_MODE, 0))

    val repeatMode: LiveData<Int> = _repeatMode.asLiveData()
    val metadata: LiveData<MediaMetadataCompat?> = _metadata.asLiveData()
    val playbackState: LiveData<PlaybackStateCompat?> = _playBackState.asLiveData()

    /***************************************/
    /**     PIN： 获取正在播放的歌单        **/
    /***************************************/
    /**
     * 1.当前正在播放的歌单的ID
     */
    private val _nowPlaylistId: MutableStateFlow<Long> =
        MutableStateFlow(sharedPref.getLong(LAST_PLAYLIST_ID, 0L))

    /**
     * 2.根据当前歌单ID从数据库获取对应歌单的歌曲并转换成 List<MediaMetadataCompat>
     */
    private val _nowPlaylistMetadata: Flow<List<MediaMetadataCompat>> =
        _nowPlaylistId.flatMapLatest {
            database.playlistDao().getByIdFlow(it).mapLatest { playlist ->
                playlist?.songs?.map { song ->
                    song.toMediaMetadataCompat()
                } ?: ArrayList()
            }
        }

    /**
     * 3.将 List<MediaMetadataCompat> 公开给其他位置使用
     */
    val nowPlaylistMetadataFlow: Flow<List<MediaMetadataCompat>> = _nowPlaylistMetadata

    /**
     *  4.将 MediaMetadataCompat 转换成 MediaItem
     */
    private val _nowPlaylistMediaItemFlow: Flow<List<MediaBrowserCompat.MediaItem>> =
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

    /**
     * 5.将 List<MediaBrowserCompat.MediaItem> 公开给其他位置使用
     */
    val nowPlaylistMediaItemLiveData: LiveData<List<MediaBrowserCompat.MediaItem>> =
        _nowPlaylistMediaItemFlow.asLiveData()


    /***************************************/
    /**    PIN： 获取所有歌单并转换         **/
    /***************************************/
    /**
     * 1.从数据库获取所有歌单
     */
    private val _allPlaylist: Flow<List<PlaylistWithSongs>> = database.playlistDao().getAllFlow()

    /**
     * 2.将从数据库获取到的 List<PlaylistWithSongs> 转换成 List<FirstNode<MPlaylist>>
     */
    val allPlaylist: LiveData<List<FirstNode<MPlaylist>>> =
        _allPlaylist.mapLatest { playlists ->
            playlists.map { playlist ->
                FirstNode(playlist.songs.map { song ->
                    SecondNode(null, song)
                }, playlist.playlist)
            }
        }.asLiveData()

    /***************************************/
    /**      PIN： 持续计算播放进度         **/
    /***************************************/
    /**
     * 根据playbackState获取最新的进度
     */
    private val _songPosition: Flow<Long> = _playBackState.flatMapLatest {
        flow {
            it ?: return@flow
            var currentDuration = Mathf.getPositionFromPlaybackStateCompat(it)

            if (it.state == PlaybackStateCompat.STATE_STOPPED) {
                notificationManager.clearLyric()
                emit(currentDuration)
                return@flow
            }

            if (it.state == PlaybackStateCompat.STATE_PLAYING) {
                repeat(Int.MAX_VALUE) {
                    emit(currentDuration)
                    currentDuration += 1000
                    delay(1000)
                }
            } else {
                emit(currentDuration)
                // 暂停时取消状态栏歌词
                notificationManager.clearLyric()
            }
        }
    }.map {
        with(sharedPref.edit()) {
            this.putLong(Config.LAST_POSITION, it)
            this.apply()
        }
        return@map it
    }
    val songPosition: LiveData<Long> = _songPosition.asLiveData()


    /***************************************/
    /**  PIN： 获取歌词并显示状态栏歌词      **/
    /***************************************/
    /**
     * 1.从数据库获取指定歌曲的歌词
     */
    private val _songDetail: Flow<MSongDetail?> = _metadata.flatMapLatest {
        database.songDetailDao().getByIdStrFlow(it?.description?.mediaId ?: "0")
    }

    /**
     * 2.将 MSongDetail 公开给其他位置使用
     */
    val songDetail: LiveData<MSongDetail?> = _songDetail.asLiveData()

    /**
     * 3.解析歌词为 List<LyricEntry>
     */
    private val _songLyrics: Flow<List<LyricEntry>?> = _songDetail.mapLatest {
        // 歌词切换时清除通知
        notificationManager.clearLyric()
        return@mapLatest LyricUtil.parseLrc(arrayOf(it?.songLyric, it?.songLyric))
    }

    /**
     * 4.根据播放时间二分查找找到当前该显示的歌词语句 LyricEntry
     */
    private var lastLyric: String? = ""
    private var lastIndex: Int = 0
    private val _singleLyric = _songLyrics.combine(_songPosition) { list, time ->
        val index = findShowLine(list, time)
        val lyricEntry = list?.let {
            if (it.isEmpty()) null else it[index]
        }
        val nowLyric = lyricEntry?.text ?: lyricEntry?.secondText
        if (nowLyric == lastLyric && index == lastIndex)
            return@combine null

        lastIndex = index
        lastLyric = nowLyric
        return@combine nowLyric
    }

    /**
     * 5.提取 LyricEntry 的歌词为 String
     */
    private val singleLyric = _singleLyric.flatMapLatest {
        flow {
            it?.let { emit(it) }
        }
    }

    /**
     * 6.collect，使该Flow运作起来
     */
    init {
        launch {
            singleLyric.collect {
                notificationManager.updateLyric(it)
            }
        }
    }

    /**
     * 二分查找算法 直接摘抄LyricUtil里的
     */
    private fun findShowLine(list: List<LyricEntry>?, time: Long): Int {
        if (list == null || list.isEmpty()) return 0
        var left = 0
        var right = list.size
        while (left <= right) {
            val middle = (left + right) / 2
            val middleTime = list[middle].time
            if (time < middleTime) {
                right = middle - 1
            } else {
                if (middle + 1 >= list.size || time < list[middle + 1].time) {
                    return middle
                }
                left = middle + 1
            }
        }
        return 0
    }
}