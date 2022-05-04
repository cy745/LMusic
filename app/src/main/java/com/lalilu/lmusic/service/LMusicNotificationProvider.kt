package com.lalilu.lmusic.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.MediaNotification
import androidx.palette.graphics.Palette
import com.lalilu.R
import com.lalilu.common.getAutomaticColor
import com.lalilu.lmusic.manager.LyricManager
import com.lalilu.lmusic.manager.SpManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
@UnstableApi
class LMusicNotificationProvider @Inject constructor(
    @ApplicationContext private val mContext: Context
) : MediaNotification.Provider, LyricManager.LyricPusher, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val notificationManager: NotificationManager = ContextCompat.getSystemService(
        mContext, NotificationManager::class.java
    ) as NotificationManager

    private val defaultIconResId: Int by lazy {
        val appIcon = mContext.applicationInfo.icon
        if (appIcon != 0) appIcon else R.drawable.ic_launcher_foreground
    }

    companion object {
        const val NOTIFICATION_ID_PLAYER = 7
        const val NOTIFICATION_ID_LOGGER = 6

        const val NOTIFICATION_CHANNEL_NAME_PLAYER = "LMusic Player"
        const val NOTIFICATION_CHANNEL_NAME_LOGGER = "LMusic Logger"

        const val NOTIFICATION_CHANNEL_ID_PLAYER = "${NOTIFICATION_CHANNEL_NAME_PLAYER}_ID"
        const val NOTIFICATION_CHANNEL_ID_LOGGER = "${NOTIFICATION_CHANNEL_NAME_LOGGER}_ID"

        const val FLAG_ALWAYS_SHOW_TICKER = 0x1000000
        const val FLAG_ONLY_UPDATE_TICKER = 0x2000000

        const val CUSTOM_ACTION = "custom_action"
        const val CUSTOM_ACTION_TOGGLE_REPEAT_MODE = "custom_action_toggle_repeat_mode"
        const val CUSTOM_ACTION_CLOSE_APPLICATION = "custom_action_close_application"
    }

    private val placeHolder = BitmapFactory.decodeResource(
        mContext.resources, R.drawable.cover_placeholder
    )

    private var notificationBgColor = 0

    private var mediaNotification: MediaNotification? = null
    private var callback: MediaNotification.Provider.Callback? = null

    private var sentenceFlow: MutableStateFlow<String?> =
        MutableStateFlow(null)

    override fun createNotification(
        mediaController: MediaController,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback
    ): MediaNotification {
        callback = onNotificationChangedCallback
        ensureNotificationChannel()

        val builder = NotificationCompat.Builder(
            mContext, NOTIFICATION_CHANNEL_ID_PLAYER
        )

        val icon = if (mediaController.shuffleModeEnabled) {
            R.drawable.ic_shuffle_line
        } else {
            when (mediaController.repeatMode) {
                Player.REPEAT_MODE_ONE -> R.drawable.ic_repeat_one_line
                Player.REPEAT_MODE_ALL -> R.drawable.ic_order_play_line
                else -> R.drawable.ic_order_play_line
            }
        }

        val text = if (mediaController.shuffleModeEnabled) {
            R.string.text_button_shuffle_on
        } else {
            when (mediaController.repeatMode) {
                Player.REPEAT_MODE_ONE -> R.string.text_button_repeat_one
                Player.REPEAT_MODE_ALL -> R.string.text_button_repeat_all
                else -> R.string.text_button_repeat_all
            }
        }

//        builder.addAction(
//            actionFactory.createCustomAction(
//                IconCompat.createWithResource(mContext, icon),
//                mContext.resources.getString(text),
//                CUSTOM_ACTION_TOGGLE_REPEAT_MODE, Bundle.EMPTY
//            )
//        )

        builder.addAction(
            actionFactory.createMediaAction(
                IconCompat.createWithResource(mContext, R.drawable.ic_skip_back_line),
                mContext.resources.getString(R.string.text_button_previous),
                MediaNotification.ActionFactory.COMMAND_SKIP_TO_PREVIOUS
            )
        )

        if (mediaController.playWhenReady) {
            builder.addAction(
                actionFactory.createMediaAction(
                    IconCompat.createWithResource(mContext, R.drawable.ic_pause_line),
                    mContext.resources.getString(R.string.text_button_pause),
                    MediaNotification.ActionFactory.COMMAND_PAUSE
                )
            )
        } else {
            builder.addAction(
                actionFactory.createMediaAction(
                    IconCompat.createWithResource(mContext, R.drawable.ic_play_line),
                    mContext.resources.getString(R.string.text_button_play),
                    MediaNotification.ActionFactory.COMMAND_PLAY
                )
            )
        }

        builder.addAction(
            actionFactory.createMediaAction(
                IconCompat.createWithResource(mContext, R.drawable.ic_skip_forward_line),
                mContext.resources.getString(R.string.text_button_next),
                MediaNotification.ActionFactory.COMMAND_SKIP_TO_NEXT
            )
        )

//        builder.addAction(
//            actionFactory.createCustomAction(
//                IconCompat.createWithResource(mContext, R.drawable.ic_close_line),
//                mContext.resources.getString(R.string.text_button_close),
//                CUSTOM_ACTION_CLOSE_APPLICATION, Bundle.EMPTY
//            )
//        )

        val stopIntent = actionFactory.createMediaActionPendingIntent(
            MediaNotification.ActionFactory.COMMAND_STOP
        )

        val mediaStyle = MediaStyle()
            .setCancelButtonIntent(stopIntent)
            .setShowCancelButton(true)
            .setShowActionsInCompactView(2)

        val metadata = mediaController.mediaMetadata

        builder.setContentIntent(mediaController.sessionActivity)
            .setDeleteIntent(stopIntent)
            .setOnlyAlertOnce(true)
            .setContentTitle(metadata.title)
            .setContentText(metadata.artist)
            .setSubText(metadata.albumTitle)
            .setColor(notificationBgColor)
            .setLargeIcon(placeHolder)
            .setStyle(mediaStyle)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(false)

        builder.setSmallIcon(
            if (Build.VERSION.SDK_INT <= 25) {
                R.drawable.media3_notification_small_icon
            } else {
                defaultIconResId
            }
        )

        metadata.artworkData?.let {
            launch(Dispatchers.IO) {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    ?: return@launch
                val palette = Palette.from(bitmap).generate()
                notificationBgColor = palette.getAutomaticColor()

                builder.setLargeIcon(bitmap)
                builder.color = notificationBgColor

                mediaNotification = MediaNotification(
                    NOTIFICATION_ID_PLAYER,
                    builder.build()
                )
                onNotificationChangedCallback.onNotificationChanged(mediaNotification!!)
            }
        }
        mediaNotification = MediaNotification(
            NOTIFICATION_ID_PLAYER,
            builder.build()
        )
        return mediaNotification!!
    }

    override fun handleCustomAction(
        mediaController: MediaController,
        action: String,
        extras: Bundle
    ) {
        if (action == CUSTOM_ACTION_TOGGLE_REPEAT_MODE) {
            if (mediaController.shuffleModeEnabled) {
                mediaController.shuffleModeEnabled = false
            } else {
                mediaController.repeatMode = when (mediaController.repeatMode) {
                    Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                    Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                    else -> Player.REPEAT_MODE_ALL
                }
            }
        }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) return

        if (notificationManager.getNotificationChannel(
                NOTIFICATION_CHANNEL_ID_PLAYER
            ) == null
        ) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID_PLAYER,
                    NOTIFICATION_CHANNEL_NAME_PLAYER,
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        if (notificationManager.getNotificationChannel(
                NOTIFICATION_CHANNEL_ID_LOGGER
            ) == null
        ) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID_LOGGER,
                    NOTIFICATION_CHANNEL_NAME_LOGGER,
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    override fun clearLyric() {
        pushLyric(null)
    }

    private var lyricPusherEnable = false
    private var sentenceToPush: String? = null
    override fun pushLyric(sentence: String?) {
        sentenceToPush = sentence
        sentenceFlow.tryEmit(if (lyricPusherEnable) sentenceToPush else null)
    }

    init {
        sentenceFlow.onEach { sentence ->
            mediaNotification ?: return@onEach
            mediaNotification!!.notification.apply {
                tickerText = sentence
                flags = flags.or(FLAG_ALWAYS_SHOW_TICKER)
                flags = flags.or(FLAG_ONLY_UPDATE_TICKER)
            }
            ensureNotificationChannel()
            callback?.onNotificationChanged(mediaNotification!!)
        }.launchIn(this)

        SpManager.listen("KEY_SETTINGS_status_bar_lyric", SpManager.SpBoolListener {
            lyricPusherEnable = it
            pushLyric(sentenceToPush)
        })
    }
}