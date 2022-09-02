package com.lalilu.lmusic.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import com.lalilu.R
import com.lalilu.common.getAutomaticColor
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datasource.MDataBase
import com.lalilu.lmusic.utils.extension.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/**
 *
 */
@Singleton
class LMusicNotification @Inject constructor(
    @ApplicationContext private val mContext: Context,
    private val database: MDataBase
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    companion object {
        const val NOTIFICATION_PLAYER_ID = 7
        const val NOTIFICATION_LOGGER_ID = 8

        const val playerChannelName = "LMusic Player"
        const val loggerChannelName = "LMusic Logger"

        const val FLAG_ALWAYS_SHOW_TICKER = 0x1000000
        const val FLAG_ONLY_UPDATE_TICKER = 0x2000000
    }

    private var channels = listOf(
        playerChannelName,
        loggerChannelName
    )

    private val mSingleRepeatAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_repeat_one_line, "single_repeat",
        buildServicePendingIntent(mContext, 1,
            Intent(mContext, LMusicService::class.java).also {
                it.putExtra(
                    PlaybackStateCompat.ACTION_SET_REPEAT_MODE.toString(),
                    PlaybackStateCompat.REPEAT_MODE_ALL
                )
            })
    )
    private val mOrderPlayAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_order_play_line, "order_play",
        buildServicePendingIntent(mContext, 2,
            Intent(mContext, LMusicService::class.java).also {
                it.putExtra(
                    PlaybackStateCompat.ACTION_SET_REPEAT_MODE.toString(),
                    PlaybackStateCompat.REPEAT_MODE_ONE
                )
            })
    )
    private val mPlayAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_play_line, "play",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            mContext, PlaybackStateCompat.ACTION_PLAY
        )
    )
    private val mPauseAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_pause_line, "pause",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            mContext, PlaybackStateCompat.ACTION_PAUSE
        )
    )
    private val mNextAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_skip_forward_line, "next",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            mContext, PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        )
    )
    private val mPrevAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_skip_back_line, "previous",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            mContext, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )
    )
    private val mStopAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_close_line, "stop",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            mContext, PlaybackStateCompat.ACTION_STOP
        )
    )

    private fun buildServicePendingIntent(
        context: Context,
        requestCode: Int,
        intent: Intent
    ): PendingIntent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            PendingIntent.getForegroundService(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        else PendingIntent.getService(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private val notificationManager: NotificationManager = ContextCompat.getSystemService(
        mContext, NotificationManager::class.java
    ) as NotificationManager

    private var currentIsStop: Boolean = true
    private var currentLyricTemp: String? = null

    @Volatile
    private var lastNotificationBuilder: NotificationCompat.Builder? = null
    private var lastBitmap: Pair<LSong, Bitmap>? = null
    private var lastColor: Pair<LSong, Int>? = null

    /**
     *  API 26 以上需要注册Channel，否则不显示通知。
     */
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        notificationManager.cancelAll()
        launch {
            LMusicLyricManager.currentSentence.collectLatest {
                currentLyricTemp = it
                updateLyric(it)
            }
        }
    }

    suspend fun updateNotification(
        data: Any?,
        service: Service,
        mediaSession: MediaSessionCompat,
    ) {
        val controller = mediaSession.controller
        val token = mediaSession.sessionToken
        val state = controller.playbackState
        val metadata = controller.metadata ?: return
        val description = metadata.description ?: return
        val repeatMode = mediaSession.controller.repeatMode
        val isPlaying = state.state == PlaybackStateCompat.STATE_PLAYING

        val style = MediaStyle()
            .setMediaSession(token)
            .setShowActionsInCompactView(0, 1, 2)
            .setShowCancelButton(true)
            .setCancelButtonIntent(mStopAction.actionIntent)

        val builder = NotificationCompat.Builder(mContext, playerChannelName + "_ID")
        builder.setStyle(style)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDeleteIntent(mStopAction.actionIntent)
            .setContentIntent(controller.sessionActivity)
            .setContentTitle(description.title)
            .setContentText(description.subtitle)
            .setSubText(description.description)
            .setShowWhen(false)
            .setAutoCancel(false)
            .setOngoing(false)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDeleteIntent(mStopAction.actionIntent)

        builder.addAction(
            when (repeatMode) {
                PlaybackStateCompat.REPEAT_MODE_ALL -> mOrderPlayAction
                PlaybackStateCompat.REPEAT_MODE_ONE -> mSingleRepeatAction
                else -> mOrderPlayAction
            }
        )
        builder.addAction(mPrevAction)
        builder.addAction(if (isPlaying) mPauseAction else mPlayAction)
        builder.addAction(mNextAction)
        builder.addAction(mStopAction)

        withContext(Dispatchers.IO) {
            if (data is LSong) builder.loadCoverAndPalette(data)

            lastNotificationBuilder = builder
            pushNotification(isPlaying, service, builder.build())
            currentIsStop = false
        }
    }

    private fun NotificationCompat.Builder.loadCoverAndPalette(data: LSong) {
        var bitmap: Bitmap? = null
        var color: Int = Color.TRANSPARENT
        lastBitmap?.takeIf { it.first == data }
            ?.let { bitmap = it.second }
        lastColor?.takeIf { it.first == data }
            ?.let { color = it.second }

        if (bitmap == null) {
            val coverData = database.networkDataDao().getById(data.id)
                ?.requireCoverUri()
                ?: data

            runBlocking {
                bitmap = this@LMusicNotification.mContext.imageLoader.execute(
                    ImageRequest.Builder(this@LMusicNotification.mContext)
                        .allowHardware(false)
                        .data(coverData)
                        .size(400)
                        .build()
                ).drawable?.toBitmap() ?: return@runBlocking
            }

            if (bitmap != null) {
                color = Palette.from(bitmap!!)
                    .generate()
                    .getAutomaticColor()

                lastBitmap = data to bitmap!!
                lastColor = data to color
            }
        }

        if (bitmap != null) {
            this@loadCoverAndPalette.setLargeIcon(bitmap)
            this@loadCoverAndPalette.color = color
        }
    }

    private fun pushNotification(isPlaying: Boolean, service: Service, notification: Notification) {
        if (isPlaying) {
            service.startForeground(NOTIFICATION_PLAYER_ID, notification)
        } else {
            notificationManager.notify(NOTIFICATION_PLAYER_ID, notification)
        }
    }

    private fun updateLyric(text: String?) {
        if (currentIsStop) return
        println("updateLyric: $text")
        lastNotificationBuilder?.build()?.apply {
            tickerText = text
            flags = flags.or(FLAG_ALWAYS_SHOW_TICKER)
            notificationManager.notify(NOTIFICATION_PLAYER_ID, this)
        }
    }

    fun stop() {
        currentIsStop = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        channels.forEach { name ->
            val id = "${name}_ID"
            if (notificationManager.getNotificationChannel(id) == null) {
                val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW)
                channel.description = "【LMusic通知频道】：$name"
                channel.importance = NotificationManager.IMPORTANCE_LOW
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                channel.setShowBadge(false)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

}