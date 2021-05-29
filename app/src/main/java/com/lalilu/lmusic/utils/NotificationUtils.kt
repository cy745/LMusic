package com.lalilu.lmusic.utils

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.lalilu.lmusic.MainActivity
import com.lalilu.lmusic.R
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

fun NotificationManager.sendNotification(
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

