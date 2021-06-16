package com.lalilu.lmusic.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import androidx.palette.graphics.Palette
import com.lalilu.lmusic.R
import com.lalilu.lmusic.utils.getAutomaticColor


class LMusicNotificationManager constructor(private val mService: Context) {
    companion object {
        const val NOTIFICATION_ID = 7
        const val playerChannelName = "LMusic Player"
        const val loggerChannelName = "LMusic Logger"
    }

    private var mPlayAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_play_line, "play",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            mService, PlaybackStateCompat.ACTION_PLAY
        )
    )
    private var mPauseAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_pause_line, "pause",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            mService, PlaybackStateCompat.ACTION_PAUSE
        )
    )
    private var mNextAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_skip_forward_line, "next",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            mService, PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        )
    )
    private var mPrevAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_skip_back_line, "previous",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            mService, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )
    )
    val notificationManager: NotificationManager =
        ContextCompat.getSystemService(
            mService, NotificationManager::class.java
        ) as NotificationManager


    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel()
        notificationManager.cancelAll()
    }

    fun getNotification(
        mediaSession: MediaSessionCompat,
    ): Notification? {
        val controller = mediaSession.controller
        val token = mediaSession.sessionToken
        val metadata = controller.metadata
        val state = controller.playbackState
        val description = metadata.description
        val isPlaying = state.state == PlaybackStateCompat.STATE_PLAYING
        val builder: NotificationCompat.Builder =
            buildNotification(controller, token!!, isPlaying, description)
        return builder.build()
    }

    private fun buildNotification(
        controller: MediaControllerCompat,
        token: MediaSessionCompat.Token,
        isPlaying: Boolean,
        description: MediaDescriptionCompat
    ): NotificationCompat.Builder {
        val bitmap = loadBitmapFromUri(description.iconUri!!, 400)
        val palette = if (bitmap != null) Palette.from(bitmap).generate() else null
        val color = palette.getAutomaticColor()
        val cancelButtonIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
            mService, PlaybackStateCompat.ACTION_STOP
        )
        val style = MediaStyle().setMediaSession(token)
            .setShowActionsInCompactView(0, 1, 2)
            .setShowCancelButton(true)
            .setCancelButtonIntent(cancelButtonIntent)
        val builder = NotificationCompat.Builder(mService, playerChannelName + "_ID")
        builder.setStyle(style)
            .setColor(color)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(controller.sessionActivity)
            .setContentTitle(description.title)
            .setContentText(description.subtitle)
            .setSubText(description.description)
            .setShowWhen(false)
            .setAutoCancel(false)
            .setLargeIcon(bitmap)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDeleteIntent(cancelButtonIntent)

        builder.addAction(mPrevAction)
        builder.addAction(if (isPlaying) mPauseAction else mPlayAction)
        builder.addAction(mNextAction)
        return builder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        val name = playerChannelName
        val id = playerChannelName + "_ID"
        if (notificationManager.getNotificationChannel(id) == null) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(id, name, importance)
            mChannel.description = "播放器控制器通道，关闭后将无法从在状态栏上控制播放器。"
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.setShowBadge(false)
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    private fun loadBitmapFromUri(uri: Uri, toSize: Int): Bitmap? {
        val outOption = BitmapFactory.Options().also {
            it.inJustDecodeBounds = true
        }
        try {
            BitmapFactory.decodeStream(uri.toFile().inputStream(), null, outOption)
        } catch (e: Exception) {
            return null
        }

        val outWidth = outOption.outWidth
        val outHeight = outOption.outHeight
        if (outWidth == -1 || outHeight == -1) return null

        var scaleValue: Int = if (outWidth > toSize) outWidth / toSize else toSize / outWidth
        if (scaleValue < 1) scaleValue = 1

        outOption.also {
            it.inJustDecodeBounds = false
            it.inSampleSize = scaleValue
        }

        return BitmapFactory.decodeStream(uri.toFile().inputStream(), null, outOption)
    }
}