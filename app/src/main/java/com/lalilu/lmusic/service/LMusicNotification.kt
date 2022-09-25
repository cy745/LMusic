package com.lalilu.lmusic.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import com.lalilu.R
import com.lalilu.common.getAutomaticColor
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.utils.PlayMode
import com.lalilu.lmusic.utils.extension.toBitmap
import kotlinx.coroutines.runBlocking

abstract class LMusicNotification(
    private val mContext: Context
) {
    private var lastBitmap: Pair<Any, Bitmap>? = null
    private var lastColor: Pair<Any, Int>? = null

    abstract fun fillData(data: Any?): Any?
    abstract fun getIsPlaying(): Boolean
    abstract fun getIsStop(): Boolean
    abstract fun getPosition(): Long
    abstract fun getService(): Service

    protected val notificationManager: NotificationManager by lazy {
        ContextCompat.getSystemService(
            mContext, NotificationManager::class.java
        ) as NotificationManager
    }

    /**
     * 加载歌曲封面和提取配色，若已有缓存则直接取用，若无则阻塞获取，需确保调用方不阻塞主要动作
     */
    protected fun NotificationCompat.Builder.loadCoverAndPalette(data: Any?): NotificationCompat.Builder {
        var bitmap: Bitmap? = null
        var color: Int = Color.TRANSPARENT
        lastBitmap?.takeIf { it.first == data }?.let { bitmap = it.second }
        lastColor?.takeIf { it.first == data }?.let { color = it.second }

        if (bitmap == null) {
            val coverData = fillData(data)

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

                if (data != null) {
                    lastBitmap = data to bitmap!!
                    lastColor = data to color
                }
            }
        }

        if (bitmap != null) {
            this@loadCoverAndPalette.setLargeIcon(bitmap)
            this@loadCoverAndPalette.color = color
        }

        return this
    }

    /**
     * 通过mediaSession创建Notification
     */
    protected fun buildNotification(mediaSession: MediaSessionCompat): NotificationCompat.Builder? {
        val style = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 2, 3)
            .setShowCancelButton(true)
            .setCancelButtonIntent(mStopAction.actionIntent)
        val controller = mediaSession.controller
        val metadata = controller.metadata ?: return null
        val description = metadata.description ?: return null
        val repeatMode = mediaSession.controller.repeatMode
        val shuffleMode = mediaSession.controller.shuffleMode
        val isPlaying = getIsPlaying()

        return NotificationCompat.Builder(
            mContext, LMusicNotificationImpl.playerChannelName + "_ID"
        ).setStyle(style)
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
            .addAction(
                when (PlayMode.of(repeatMode = repeatMode, shuffleMode = shuffleMode)) {
                    PlayMode.Shuffle -> mShufflePlayAction
                    PlayMode.RepeatOne -> mSingleRepeatAction
                    else -> mOrderPlayAction
                }
            )
            .addAction(mPrevAction)
            .addAction(if (isPlaying) mPauseAction else mPlayAction)
            .addAction(mNextAction)
            .addAction(mStopAction)

    }

    protected fun pushNotification(notification: Notification) {
        if (getIsStop()) return
        if (getIsPlaying()) {
            getService()
                .startForeground(LMusicNotificationImpl.NOTIFICATION_PLAYER_ID, notification)
        } else {
            notificationManager.notify(LMusicNotificationImpl.NOTIFICATION_PLAYER_ID, notification)
        }
    }

    private val mOrderPlayAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_order_play_line, "order_play",
        buildServicePendingIntent(
            mContext, 1,
            Intent(mContext, LMusicService::class.java)
                .setAction(Config.ACTION_SET_REPEAT_MODE)
                .putExtra(PlayMode.KEY, PlayMode.ListRecycle.next().value)
        )
    )
    private val mSingleRepeatAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_repeat_one_line, "single_repeat",
        buildServicePendingIntent(
            mContext, 2,
            Intent(mContext, LMusicService::class.java)
                .setAction(Config.ACTION_SET_REPEAT_MODE)
                .putExtra(PlayMode.KEY, PlayMode.RepeatOne.next().value)
        )
    )
    private val mShufflePlayAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_shuffle_line, "shuffle_play",
        buildServicePendingIntent(
            mContext, 3,
            Intent(mContext, LMusicService::class.java)
                .setAction(Config.ACTION_SET_REPEAT_MODE)
                .putExtra(PlayMode.KEY, PlayMode.Shuffle.next().value)
        )
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
}