package com.lalilu.lmusic.service.notification

import android.app.Notification
import android.app.Service
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import com.lalilu.lmusic.datastore.SettingsDataStore
import com.lalilu.lmusic.repository.CoverRepository
import com.lalilu.lmusic.repository.LyricRepository
import com.lalilu.lmusic.service.playback.impl.MixPlayback
import com.lalilu.lmusic.service.pusher.Pusher
import com.lalilu.lmusic.utils.extension.getMediaId
import com.lalilu.lmusic.utils.extension.toUpdatableFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class LMusicNotifier constructor(
    lyricRepo: LyricRepository,
    playback: MixPlayback,
    coverRepo: CoverRepository,
    settingsDataStore: SettingsDataStore,
    val mediaSession: MediaSessionCompat,
    val service: Service
) : BaseNotification(service), CoroutineScope, Pusher {
    override val coroutineContext: CoroutineContext = Dispatchers.Default + SupervisorJob()

    /**
     * 创建基础的Notification.Builder,从mediaSession读取基础数据填充
     */
    private val notificationBuilderFlow = flow {
        val builder = buildMediaNotification(
            mediaSession = mediaSession,
            channelId = PLAYER_CHANNEL_ID
        )
        emit(builder)
    }.toUpdatableFlow()

    private var getIsPlaying: () -> Boolean = { playback.player?.isPlaying ?: false }
    private var getIsStop: () -> Boolean = { playback.player?.isStopped ?: true }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(PLAYER_CHANNEL_ID, PLAYER_CHANNEL_NAME)
        }
        notificationManager.cancelAll()

        notificationBuilderFlow.debounce(50).flatMapLatest { builder ->
            val mediaId = mediaSession.getMediaId()

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
        }.combine(settingsDataStore.run { enableStatusLyric.flow() }) { notification, enable ->
            notification?.let {
                if (enable != true) {
                    it.tickerText = null
                    it.flags = it.flags and FLAG_ALWAYS_SHOW_TICKER.inv()
                    it.flags = it.flags and FLAG_ONLY_UPDATE_TICKER.inv()
                }
                return@let it
            }
        }
            .debounce(50)
            .mapLatest { it?.let(this::pushNotification) }
            .launchIn(this)
    }

    override fun update() {
        notificationBuilderFlow.requireUpdate()
    }

    override fun cancel() {
        cancelNotification(notificationId = NOTIFICATION_PLAYER_ID)
    }

    override fun destroy() {

    }

    override fun pushNotification(notification: Notification) {
        if (getIsStop()) return

        if (getIsPlaying()) {
            service.startForeground(NOTIFICATION_PLAYER_ID, notification)
        } else {
            super.pushNotification(notification)
        }
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