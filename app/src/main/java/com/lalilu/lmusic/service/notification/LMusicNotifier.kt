package com.lalilu.lmusic.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import com.lalilu.lmusic.repository.LyricRepository
import com.lalilu.lmusic.service.playback.MixPlayback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@Singleton
class LMusicNotifier @Inject constructor(
    @ApplicationContext mContext: Context,
    lyricRepo: LyricRepository,
    playback: MixPlayback
) : BaseNotification(mContext), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default + SupervisorJob()
    private val lastPushTimeFlow = MutableStateFlow(System.currentTimeMillis())

    var getMediaSession: (() -> MediaSessionCompat?)? = null
    var getService: (() -> Service?)? = null
    private var getIsPlaying: () -> Boolean = playback.player::isPlaying
    private var getIsStop: () -> Boolean = playback.player::isStopped

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        notificationManager.cancelAll()

        lastPushTimeFlow.mapLatest {
            val mediaSessionCompat = getMediaSession?.invoke() ?: return@mapLatest null
            buildMediaNotification(
                mediaSession = mediaSessionCompat,
                channelId = PLAYER_CHANNEL_ID
            )
        }.debounce(50)
            .mapLatest { it?.build()?.also(this::pushNotification) }
            .combine(lyricRepo.currentLyricSentence) { notification, sentence ->
                notification?.apply {
                    tickerText = sentence
                    flags = flags.or(FLAG_ALWAYS_SHOW_TICKER)
                    flags = flags.or(FLAG_ONLY_UPDATE_TICKER)
                }
            }.debounce(50)
            .mapLatest { it?.let(this::pushNotification) }
            .launchIn(this)
    }

    fun update() {
        lastPushTimeFlow.tryEmit(System.currentTimeMillis())
    }

    fun cancel() {
        cancelNotification(notificationId = NOTIFICATION_PLAYER_ID)
    }

    override fun pushNotification(notification: Notification) {
        val isStopped = getIsStop.invoke()
        if (isStopped) return

        val service = getService?.invoke()
        val isPlaying = getIsPlaying.invoke()
        if (service != null && isPlaying) {
            service.startForeground(NOTIFICATION_PLAYER_ID, notification)
        } else {
            super.pushNotification(notification)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            PLAYER_CHANNEL_ID,
            PLAYER_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "【LMusic通知频道】：${PLAYER_CHANNEL_NAME}"
            importance = NotificationManager.IMPORTANCE_LOW
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
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