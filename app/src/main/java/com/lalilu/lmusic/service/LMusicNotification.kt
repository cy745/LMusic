package com.lalilu.lmusic.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import com.lalilu.R
import com.lalilu.common.getAutomaticColor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 *
 */
@Singleton
class LMusicNotification @Inject constructor(
    @ApplicationContext private val mContext: Context
) {
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


    /**
     *  API 26 以上需要注册Channel，否则不显示通知。
     */
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        notificationManager.cancelAll()
    }

    suspend fun updateNotification(
        mediaSession: MediaSessionCompat,
        data: Any? = null,
        startForeground: ((Notification) -> Unit)? = null
    ) {
        val controller = mediaSession.controller
        val token = mediaSession.sessionToken
        val metadata = controller.metadata
        val state = controller.playbackState
        val description = metadata.description
        val repeatMode = mediaSession.controller.repeatMode
        val isPlaying = state.state == PlaybackStateCompat.STATE_PLAYING

        val style = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(token)
            .setShowActionsInCompactView(0, 1, 2)
            .setShowCancelButton(true)
            .setCancelButtonIntent(mStopAction.actionIntent)

        val builder = NotificationCompat.Builder(mContext, playerChannelName + "_ID")
        builder.setStyle(style)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(controller.sessionActivity)
            .setContentTitle(description.title)
            .setContentText(description.subtitle)
            .setSubText(description.description)
            .setShowWhen(false)
            .setAutoCancel(false)
            .setOngoing(true)
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
            mContext.imageLoader.execute(
                ImageRequest.Builder(mContext)
                    .allowHardware(false)
                    .data(data)
                    .build()
            ).drawable.let { drawable ->
                drawable?.let { it as BitmapDrawable }?.bitmap?.let {
                    val palette = Palette.from(it).generate()
                    builder.color = palette.getAutomaticColor()
                    builder.setLargeIcon(it)
                }
                if (startForeground != null) {
                    startForeground(builder.build())
                    return@withContext
                }
                notificationManager.notify(NOTIFICATION_PLAYER_ID, builder.build())
            }
        }
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