package com.lalilu.lmusic.service

import StatusBarLyric.API.StatusBarLyric
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import com.lalilu.R
import com.lalilu.common.getAutomaticColor
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.repository.CoverRepository
import com.lalilu.lmusic.repository.LyricRepository
import com.lalilu.lmusic.utils.extension.getMediaId
import com.lalilu.lplayer.LPlayer
import com.lalilu.lplayer.extensions.isPlaying
import com.lalilu.lplayer.notification.BaseNotification
import com.lalilu.lplayer.playback.PlayMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class LMusicNotifier constructor(
    private val mContext: Context,
    private val lyricRepo: LyricRepository,
    private val coverRepo: CoverRepository,
    private val settingsSp: SettingsSp,
    private val statusBarLyric: StatusBarLyric
) : BaseNotification(mContext), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default + SupervisorJob()

    /**
     * 创建基础的Notification.Builder,从mediaSession读取基础数据填充
     */
    private val notificationBuilderFlow = MutableStateFlow<NotificationCompat.Builder?>(null)
    private var notificationLoopJob: Job? = null

    override suspend fun getBitmapFromData(data: Any?): Bitmap? {
        return mContext.imageLoader.execute(
            ImageRequest.Builder(mContext)
                .allowHardware(false)
                .data(data)
                .size(400)
                .build()
        ).drawable?.toBitmap()
    }

    override fun getColorFromBitmap(bitmap: Bitmap): Int {
        return Palette.from(bitmap)
            .generate()
            .getAutomaticColor()
    }

    override fun NotificationCompat.Builder.customActionBtn(playMode: PlayMode): NotificationCompat.Builder {
        return addAction(
            when (playMode) {
                PlayMode.ListRecycle -> mOrderPlayAction
                PlayMode.RepeatOne -> mSingleRepeatAction
                PlayMode.Shuffle -> mShufflePlayAction
            }
        )
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(PLAYER_CHANNEL_ID, PLAYER_CHANNEL_NAME)
        }
        notificationManager.cancelAll()
    }

    private val mOrderPlayAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_order_play_line, "order_play",
        buildServicePendingIntent(
            mContext, 1,
            Intent(mContext, LMusicService::class.java)
                .setAction(LPlayer.ACTION_SET_REPEAT_MODE)
                .putExtra(PlayMode.KEY, PlayMode.ListRecycle.next().value)
        )
    )
    private val mSingleRepeatAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_repeat_one_line, "single_repeat",
        buildServicePendingIntent(
            mContext, 2,
            Intent(mContext, LMusicService::class.java)
                .setAction(LPlayer.ACTION_SET_REPEAT_MODE)
                .putExtra(PlayMode.KEY, PlayMode.RepeatOne.next().value)
        )
    )
    private val mShufflePlayAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_shuffle_line, "shuffle_play",
        buildServicePendingIntent(
            mContext, 3,
            Intent(mContext, LMusicService::class.java)
                .setAction(LPlayer.ACTION_SET_REPEAT_MODE)
                .putExtra(PlayMode.KEY, PlayMode.Shuffle.next().value)
        )
    )

    private fun startLoop(mediaSession: MediaSessionCompat) {
        notificationLoopJob?.cancel()
        notificationLoopJob = notificationBuilderFlow.flatMapLatest { builder ->
            val mediaId = mediaSession.getMediaId()

            coverRepo.fetch(mediaId).mapLatest {
                builder?.loadCoverAndPalette(mediaSession, it)?.build()
            }
        }.combine(lyricRepo.currentLyricSentence) { notification, sentence ->
            notification?.setLyricTicker(sentence)
        }.combine(settingsSp.enableStatusLyric.flow(true)) { notification, enable ->
            notification?.apply {
                if (enable == true && mediaSession.isPlaying()) return@apply

                clearLyricTicker()
            }
        }
//            .debounce(50)
            .onEach {
                statusBarLyric.updateLyric(it?.tickerText?.toString() ?: "")
                notificationManager.notify(NOTIFICATION_PLAYER_ID, it)
            }.launchIn(this)
    }

    private fun stopLoop() {
        statusBarLyric.stopLyric()
        notificationLoopJob?.cancel()
        notificationLoopJob = null
    }

    override fun startForeground(
        mediaSession: MediaSessionCompat,
        callback: (Int, Notification) -> Unit
    ) {
        val builder = buildMediaNotification(
            mediaSession = mediaSession,
            channelId = PLAYER_CHANNEL_ID,
            smallIcon = R.drawable.ic_launcher_icon
        )?.loadCoverAndPalette(mediaSession, null)
            ?: return
        callback(NOTIFICATION_PLAYER_ID, builder.build())
        notificationBuilderFlow.tryEmit(builder)
        startLoop(mediaSession)
    }

    override fun stopForeground(callback: () -> Unit) {
        stopLoop()
        callback()
    }

    override fun update(mediaSession: MediaSessionCompat) {
        launch {
            notificationBuilderFlow.emit(
                buildMediaNotification(
                    mediaSession = mediaSession,
                    channelId = PLAYER_CHANNEL_ID,
                    smallIcon = R.drawable.ic_launcher_icon
                )
            )
        }
    }

    override fun cancel() {
        stopLoop()
        notificationManager.cancel(NOTIFICATION_PLAYER_ID)
    }

    private fun Notification.setLyricTicker(text: String?): Notification = apply {
        this.tickerText = text
        flags = if (flags and FLAG_ALWAYS_SHOW_TICKER != FLAG_ALWAYS_SHOW_TICKER) {
            flags or FLAG_ALWAYS_SHOW_TICKER
        } else {
            flags or FLAG_ONLY_UPDATE_TICKER
        }
    }

    private fun Notification.clearLyricTicker() {
        tickerText = null
        flags = flags and FLAG_ALWAYS_SHOW_TICKER.inv()
        flags = flags and FLAG_ONLY_UPDATE_TICKER.inv()
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