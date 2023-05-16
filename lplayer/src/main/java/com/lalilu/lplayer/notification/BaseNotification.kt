package com.lalilu.lplayer.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.lalilu.lplayer.R
import com.lalilu.lplayer.playback.PlayMode
import kotlinx.coroutines.runBlocking

abstract class BaseNotification constructor(
    private val mContext: Context,
) : Notifier {
    private var lastBitmap: Pair<Any, Bitmap>? = null
    private var lastColor: Pair<Any, Int>? = null
    private val emptyBitmap: Bitmap? by lazy {
        ContextCompat.getDrawable(mContext, R.drawable.ic_music_notification_bg_64dp)?.toBitmap()
    }

    abstract suspend fun getBitmapFromData(data: Any?): Bitmap?
    abstract fun getColorFromBitmap(bitmap: Bitmap): Int
    abstract fun NotificationCompat.Builder.customActionBtn(playMode: PlayMode): NotificationCompat.Builder


    /**
     * 加载歌曲封面和提取配色，若已有缓存则直接取用，若无则阻塞获取，需确保调用方不阻塞主要动作
     */
    protected fun NotificationCompat.Builder.loadCoverAndPalette(
        mediaSession: MediaSessionCompat?,
        data: Any?
    ): NotificationCompat.Builder = apply {
        var bitmap: Bitmap? = null
        var color: Int = Color.TRANSPARENT

        lastBitmap?.takeIf { it.first == data }?.let { bitmap = it.second }
        lastColor?.takeIf { it.first == data }?.let { color = it.second }

        if (bitmap == null) {
            if (data != null) {
                runBlocking {
                    bitmap = getBitmapFromData(data) ?: return@runBlocking
                }
            }

            if (bitmap != null) {
                color = getColorFromBitmap(bitmap!!)

                if (data != null) {
                    lastBitmap = data to bitmap!!
                    lastColor = data to color
                }
            } else {
                bitmap = emptyBitmap
                lastColor?.second?.let { color = it }
            }
        }

        if (bitmap != null) {
            if (bitmap != emptyBitmap) {
                mediaSession?.setMetadata(
                    MediaMetadataCompat.Builder(mediaSession.controller.metadata)
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap)
                        .build()
                )
            }
            this@loadCoverAndPalette.setLargeIcon(bitmap)
            this@loadCoverAndPalette.color = color
        }
    }

    fun buildMediaNotification(
        mediaSession: MediaSessionCompat,
        channelId: String,
        @DrawableRes smallIcon: Int
    ): NotificationCompat.Builder? {
        val style = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 2, 3)
            .setShowCancelButton(true)
            .setCancelButtonIntent(mStopAction.actionIntent)
        val controller = mediaSession.controller
        val metadata = controller.metadata ?: return null
        val description = metadata.description ?: return null
        val playbackState = controller.playbackState ?: return null
        val repeatMode = controller.repeatMode
        val shuffleMode = controller.shuffleMode
        val isPlaying = playbackState.state == PlaybackStateCompat.STATE_PLAYING

        return NotificationCompat.Builder(mContext, channelId)
            .setStyle(style)
            .setSmallIcon(smallIcon)
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
            .customActionBtn(PlayMode.of(repeatMode = repeatMode, shuffleMode = shuffleMode))
            .addAction(mPrevAction)
            .addAction(if (isPlaying) mPauseAction else mPlayAction)
            .addAction(mNextAction)
            .addAction(mStopAction)
    }

    protected val notificationManager: NotificationManager by lazy {
        ContextCompat.getSystemService(
            mContext, NotificationManager::class.java
        ) as NotificationManager
    }

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
        R.drawable.ic_skip_next_line, "next",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            mContext, PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        )
    )
    private val mPrevAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_skip_previous_line, "previous",
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

    fun buildServicePendingIntent(
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

    @RequiresApi(Build.VERSION_CODES.O)
    protected fun createNotificationChannel(channelID: String, channelName: String) {
        val channel = NotificationChannel(
            channelID,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = channelName
            importance = NotificationManager.IMPORTANCE_LOW
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun Drawable.toBitmap(): Bitmap {
        val w = this.intrinsicWidth
        val h = this.intrinsicHeight

        val config = Bitmap.Config.ARGB_8888
        val bitmap = Bitmap.createBitmap(w, h, config)
        val canvas = Canvas(bitmap)
        this.setBounds(0, 0, w, h)
        this.draw(canvas)
        return bitmap
    }
}