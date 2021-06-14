package com.lalilu.lmusic.notification

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.media.session.MediaButtonReceiver
import androidx.palette.graphics.Palette
import com.lalilu.lmusic.MainActivity
import com.lalilu.lmusic.R
import com.lalilu.lmusic.notification.NotificationUtils.Companion.playerChannelName
import com.lalilu.lmusic.service2.MusicService
import com.lalilu.lmusic.utils.getAutomaticColor
import com.lalilu.lmusic.viewmodel.MusicServiceViewModel

private const val NOTIFICATION_ID = 0

class NotificationUtils private constructor(application: Application) {
    companion object {
        const val playerChannelName = "LMusic Player"
        const val loggerChannelName = "LMusic Logger"

        @Volatile
        private var mInstance: NotificationUtils? = null

        fun getInstance(application: Application): NotificationUtils {
            if (mInstance == null) synchronized(MusicServiceViewModel::class.java) {
                if (mInstance == null) mInstance = NotificationUtils(application)
            }
            return mInstance!!
        }
    }

    fun getNotificationManager(): NotificationManager = notificationManager

    private val notificationManager: NotificationManager =
        ContextCompat.getSystemService(
            application, NotificationManager::class.java
        ) as NotificationManager

    init {
        createChannel(playerChannelName, playerChannelName + "_ID")
        createChannel(loggerChannelName, loggerChannelName + "_ID")
    }

    private fun createChannel(channelName: String, channelID: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelID,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(false)
//            notificationChannel.description = "Time for breakfast"
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}

fun MusicService.sendPlayerNotification(
    mediaSession: MediaSessionCompat,
) {
    val controller = mediaSession.controller
    val description = mediaSession.controller.metadata.description
    val style = androidx.media.app.NotificationCompat.MediaStyle()
        .setMediaSession(mediaSession.sessionToken)
        .setShowActionsInCompactView(0)
        .setShowCancelButton(true)
        .setCancelButtonIntent(
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                this, PlaybackStateCompat.ACTION_STOP
            )
        )
    val bitmap = loadBitmapFromUri(description.iconUri!!, 400)
    val palette = if (bitmap != null) Palette.from(bitmap).generate() else null
    val color = palette.getAutomaticColor()
    val channelId = playerChannelName + "_ID"

    val builder = NotificationCompat.Builder(this, channelId)
        .setContentTitle(description.title)
        .setContentText(description.subtitle)
        .setSubText(description.description)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentIntent(controller.sessionActivity)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setLargeIcon(bitmap)
        .setColor(color)
        .setColorized(true)
        .setOngoing(true)
        .setStyle(style)
        .setDeleteIntent(
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                this, PlaybackStateCompat.ACTION_STOP
            )
        )
    builder.addAction(
        NotificationCompat.Action(
            R.drawable.ic_skip_back_line, "skip_back",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
        )
    )
    if (controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
        builder.addAction(
            NotificationCompat.Action(
                R.drawable.ic_pause_line, "pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this, PlaybackStateCompat.ACTION_PAUSE
                )
            )
        )
    } else {
        builder.addAction(
            NotificationCompat.Action(
                R.drawable.ic_play_line, "play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this, PlaybackStateCompat.ACTION_PLAY
                )
            )
        )
    }
    builder.addAction(
        NotificationCompat.Action(
            R.drawable.ic_skip_forward_line, "skip_forward",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            )
        )
    )
    this.startForeground(10, builder.build())
}

fun loadBitmapFromUri(uri: Uri, toSize: Int): Bitmap? {
    val outOption = BitmapFactory.Options().also {
        it.inJustDecodeBounds = true
    }
    BitmapFactory.decodeStream(uri.toFile().inputStream(), null, outOption)

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

fun NotificationManager.sendPlayerNotification(
    messageBody: String,
    channelId: String,
    applicationContext: Context
) {
    val contentIntent = Intent(applicationContext, MainActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext, NOTIFICATION_ID, contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val builder = NotificationCompat.Builder(applicationContext, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(NotificationUtils.playerChannelName)
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent)

    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}

