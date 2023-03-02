package com.lalilu.lmusic.service.notification

import android.app.Notification
import android.content.Context
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.repository.CoverRepository
import com.lalilu.lmusic.repository.LyricRepository
import com.lalilu.lmusic.service.pusher.Pusher
import com.lalilu.lmusic.utils.extension.getMediaId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class LMusicNotifier constructor(
    private val lyricRepo: LyricRepository,
    private val coverRepo: CoverRepository,
    private val lMusicSp: LMusicSp,
    context: Context
) : BaseNotification(context), CoroutineScope, Pusher {
    override val coroutineContext: CoroutineContext = Dispatchers.Default + SupervisorJob()

    /**
     * 创建基础的Notification.Builder,从mediaSession读取基础数据填充
     */
    private val notificationBuilderFlow = MutableStateFlow<NotificationCompat.Builder?>(null)
    private var notificationLoopJob: Job? = null
    private var mediaSession: MediaSessionCompat? = null

    fun bindMediaSession(session: MediaSessionCompat) {
        mediaSession = session
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(PLAYER_CHANNEL_ID, PLAYER_CHANNEL_NAME)
        }
        notificationManager.cancelAll()
    }

    private fun startLoop() {
        notificationLoopJob?.cancel()
        notificationLoopJob = launch {
            notificationBuilderFlow.flatMapLatest { builder ->
                val mediaId = mediaSession?.getMediaId()

                coverRepo.fetch(mediaId).mapLatest {
                    builder?.loadCoverAndPalette(mediaSession, it)?.build()
                }
            }.combine(lyricRepo.currentLyricSentence) { notification, sentence ->
                notification?.apply {
                    tickerText = sentence
                    flags = if (flags and FLAG_ALWAYS_SHOW_TICKER != FLAG_ALWAYS_SHOW_TICKER) {
                        flags or FLAG_ALWAYS_SHOW_TICKER
                    } else {
                        flags or FLAG_ONLY_UPDATE_TICKER
                    }
                }
            }.combine(lMusicSp.enableStatusLyric.flow(true)) { notification, enable ->
                notification?.apply {
                    if (enable == true) return@apply
                    tickerText = null
                    flags = flags and FLAG_ALWAYS_SHOW_TICKER.inv()
                    flags = flags and FLAG_ONLY_UPDATE_TICKER.inv()
                }
            }
//                .debounce(50)
                .collectLatest {
                    it?.let(this@LMusicNotifier::pushNotification)
                }
        }
    }

    private fun stopLoop() {
        notificationLoopJob?.cancel()
        notificationLoopJob = null
    }

    fun startForeground(callback: (Int, Notification) -> Unit) {
        val builder = mediaSession?.let {
            buildMediaNotification(it, PLAYER_CHANNEL_ID)
                ?.loadCoverAndPalette(it, null)
        } ?: return
        callback(NOTIFICATION_PLAYER_ID, builder.build())
        notificationBuilderFlow.tryEmit(builder)
        startLoop()
    }

    fun stopForeground(callback: () -> Unit) {
        stopLoop()
        callback()
    }

    override fun update() {
        launch {
            notificationBuilderFlow.emit(
                mediaSession?.let {
                    buildMediaNotification(
                        mediaSession = it,
                        channelId = PLAYER_CHANNEL_ID
                    )
                }
            )
        }
    }

    override fun cancel() {
        stopLoop()
        cancelNotification(notificationId = NOTIFICATION_PLAYER_ID)
    }

    override fun destroy() {
        mediaSession = null
    }

    companion object {
        const val NOTIFICATION_PLAYER_ID = 7
        const val NOTIFICATION_LOGGER_ID = 8

        private const val PLAYER_CHANNEL_NAME = "LMusic Player"
        private const val LOGGER_CHANNEL_NAME = "LMusic Logger"

        const val PLAYER_CHANNEL_ID = PLAYER_CHANNEL_NAME + "_ID"
        const val LOGGER_CHANNEL_ID = PLAYER_CHANNEL_NAME + "_ID"

        const val FLAG_ALWAYS_SHOW_TICKER = 0x1000000
        const val FLAG_ONLY_UPDATE_TICKER = 0x2000000
    }
}