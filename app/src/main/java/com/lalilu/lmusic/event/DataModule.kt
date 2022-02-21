package com.lalilu.lmusic.event

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dirror.lyricviewx.LyricEntry
import com.dirror.lyricviewx.LyricUtil
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.Config.LAST_REPEAT_MODE
import com.lalilu.lmusic.database.MediaSource
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.manager.LMusicNotificationManager
import com.lalilu.lmusic.utils.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
@ExperimentalCoroutinesApi
class DataModule @Inject constructor(
    @ApplicationContext context: Context,
    notificationManager: LMusicNotificationManager,
    mediaSource: MediaSource,
) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    init {
        mediaSource.getAllSongs()
        mediaSource.getAllAlbums()
    }

    private val sharedPref = context.getSharedPreferences(
        Config.SHARED_PLAYER, Context.MODE_PRIVATE
    )

    fun updatePlaybackState(playbackStateCompat: PlaybackStateCompat?) = launch {
        _playBackState.emit(playbackStateCompat)
        playbackStateCompat?.saveTo(sharedPref)
    }

    fun updateMetadata(metadataCompat: MediaMetadataCompat?) = launch {
        _metadata.emit(metadataCompat)
        metadataCompat?.saveTo(sharedPref)
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

    val repeatModeFlow: Flow<Int> = _repeatMode
    val repeatMode: LiveData<Int> = _repeatMode.asLiveData()
    val mediaId: Flow<String?> = _metadata.mapLatest { it?.description?.mediaId }

    /***************************************/
    /**     PIN： 获取正在播放的歌单        **/
    /***************************************/
    /**
     * 根据当前歌单ID从数据库获取对应歌单的歌曲并转换成 List<MSong>
     */
    private val _nowList: Flow<List<MSong>> = mediaSource._songs

    // 3.将 List<MSong> 公开给其他位置使用
    val nowListFlow: Flow<List<MSong>> = _nowList

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
                val ticker = ticker(1000, 0)
                for (event in ticker) {
                    currentDuration += 1000
                    emit(currentDuration)
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
    private val _songLyric: Flow<String?> = _metadata.mapLatest {
        val songData = it?.bundle?.getString(Config.MEDIA_MEDIA_DATA)
        EmbeddedDataUtils.loadLyric(songData)
    }.flowOn(Dispatchers.IO)

    val songLyric: LiveData<String?> = _songLyric.asLiveData()

    /**
     * 3.解析歌词为 List<LyricEntry>
     */
    private val _songLyrics: Flow<List<LyricEntry>?> = _songLyric.mapLatest {
        // 歌词切换时清除通知
        notificationManager.clearLyric()
        return@mapLatest LyricUtil.parseLrc(arrayOf(it, null))
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
        flow { it?.let { emit(it) } }
    }

    /**
     * 6.collect，使该Flow运作起来
     */
    init {
        launch {
            singleLyric.collect { notificationManager.updateLyric(it) }
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