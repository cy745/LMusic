package com.lalilu.lmusic.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import com.blankj.utilcode.util.SPUtils
import com.google.common.collect.ImmutableList
import com.lalilu.R
import com.lalilu.common.getAutomaticColor
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.datasource.MDataBase
import com.lalilu.lmusic.manager.LyricManager
import com.lalilu.lmusic.manager.SpManager
import com.lalilu.lmusic.utils.RepeatMode
import com.lalilu.lmusic.utils.RepeatMode.*
import com.lalilu.lmusic.utils.extension.toBitmap
import com.lalilu.lmusic.utils.safeLaunch
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@UnstableApi
@Singleton
class LMusicNotificationProvider @Inject constructor(
    @ApplicationContext
    private val mContext: Context,
    private val database: MDataBase,
    private val lyricManager: LyricManager
) : MediaNotification.Provider, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val notificationManager: NotificationManager = ContextCompat.getSystemService(
        mContext, NotificationManager::class.java
    ) as NotificationManager

    private var notificationBgColor = 0
    private var mediaNotification: MediaNotification? = null
    private var lastBitmap: Pair<MediaItem, Bitmap>? = null
    private var callback: MediaNotification.Provider.Callback? = null
    private var lyricPusherEnable = MutableStateFlow(false)
    private val localSp: SPUtils by lazy {
        SPUtils.getInstance(mContext.packageName, AppCompatActivity.MODE_PRIVATE)
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

        const val CUSTOM_ACTION_TOGGLE_REPEAT_MODE = "custom_action_toggle_repeat_mode"
    }

    private val placeHolder = BitmapFactory.decodeResource(
        mContext.resources, R.drawable.cover_placeholder
    )

    private val builder = NotificationCompat.Builder(
        mContext, NOTIFICATION_CHANNEL_ID_PLAYER
    )

    override fun createNotification(
        session: MediaSession,
        customLayout: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        callback: MediaNotification.Provider.Callback
    ): MediaNotification {
        this.callback = callback
        this.builder.clearActions()
        ensureNotificationChannel()

        val repeatMode = RepeatMode.of(
            session.player.repeatMode,
            session.player.shuffleModeEnabled
        )

        val icon = when (repeatMode) {
            RepeatOne -> R.drawable.ic_repeat_one_line
            ListRecycle -> R.drawable.ic_order_play_line
            Shuffle -> R.drawable.ic_shuffle_line
        }

        val text = when (repeatMode) {
            RepeatOne -> R.string.text_button_repeat_one
            ListRecycle -> R.string.text_button_repeat_all
            Shuffle -> R.string.text_button_shuffle_on
        }

        builder.addAction(
            actionFactory.createCustomAction(
                session,
                IconCompat.createWithResource(mContext, icon),
                mContext.resources.getString(text),
                CUSTOM_ACTION_TOGGLE_REPEAT_MODE, Bundle.EMPTY
            )
        )

        builder.addAction(
            actionFactory.createMediaAction(
                session,
                IconCompat.createWithResource(mContext, R.drawable.ic_skip_back_line),
                mContext.resources.getString(R.string.text_button_previous),
                Player.COMMAND_SEEK_TO_PREVIOUS
            )
        )

        if (session.player.playWhenReady) {
            builder.addAction(
                actionFactory.createMediaAction(
                    session,
                    IconCompat.createWithResource(mContext, R.drawable.ic_pause_line),
                    mContext.resources.getString(R.string.text_button_pause),
                    Player.COMMAND_PLAY_PAUSE
                )
            )
        } else {
            builder.addAction(
                actionFactory.createMediaAction(
                    session,
                    IconCompat.createWithResource(mContext, R.drawable.ic_play_line),
                    mContext.resources.getString(R.string.text_button_play),
                    Player.COMMAND_PLAY_PAUSE
                )
            )
        }

        builder.addAction(
            actionFactory.createMediaAction(
                session,
                IconCompat.createWithResource(mContext, R.drawable.ic_skip_forward_line),
                mContext.resources.getString(R.string.text_button_next),
                Player.COMMAND_SEEK_TO_NEXT
            )
        )

        builder.addAction(
            actionFactory.createMediaAction(
                session,
                IconCompat.createWithResource(mContext, R.drawable.ic_close_line),
                mContext.resources.getString(R.string.text_button_close),
                Player.COMMAND_STOP
            )
        )

        val stopIntent = actionFactory.createMediaActionPendingIntent(
            session, Player.COMMAND_STOP.toLong()
        )

        val mediaStyle = MediaStyle()
            .setCancelButtonIntent(stopIntent)
            .setShowCancelButton(true)
            .setShowActionsInCompactView(0, 2, 3)

        val metadata = session.player.mediaMetadata

        builder.setContentIntent(session.sessionActivity)
            .setDeleteIntent(stopIntent)
            .setOnlyAlertOnce(true)
            .setContentTitle(metadata.title)
            .setContentText(metadata.artist)
            .setSubText(metadata.albumTitle)
            .setColor(notificationBgColor)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(placeHolder)
            .setStyle(mediaStyle)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(false)

        loadCoverAndPalette(session)
        return MediaNotification(NOTIFICATION_ID_PLAYER, builder.build().apply {
            flags = flags.or(FLAG_ALWAYS_SHOW_TICKER)
        })
    }

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle
    ): Boolean {
        when (action) {
            CUSTOM_ACTION_TOGGLE_REPEAT_MODE -> {
                localSp.put(
                    Config.KEY_SETTINGS_REPEAT_MODE,
                    RepeatMode.of(
                        session.player.repeatMode,
                        session.player.shuffleModeEnabled
                    ).next().ordinal
                )
                return true
            }
        }
        return false
    }

    private fun loadCoverAndPalette(session: MediaSession) = safeLaunch(Dispatchers.IO) {
        var bitmap: Bitmap? = null
        val mediaItem = withContext(Dispatchers.Main) {
            session.player.currentMediaItem
        } ?: return@safeLaunch
        lastBitmap?.takeIf { it.first == mediaItem }
            ?.let { bitmap = it.second }

        if (bitmap == null) {
            val data = database.networkDataDao()
                .getById(mediaItem.mediaId)
                ?.requireCoverUri()
                ?: Library.getSongOrNull(mediaItem.mediaId)

            bitmap = mContext.imageLoader.execute(
                ImageRequest.Builder(mContext)
                    .data(data)
                    .size(400)
                    .allowHardware(false)
                    .build()
            ).drawable?.toBitmap() ?: return@safeLaunch

            lastBitmap = mediaItem to bitmap!!
            notificationBgColor = Palette.from(bitmap!!)
                .generate()
                .getAutomaticColor()

            builder.color = Color.TRANSPARENT
        }

        if (bitmap != null) {
            builder.color = notificationBgColor
            builder.setLargeIcon(bitmap)
        }

        mediaNotification = MediaNotification(NOTIFICATION_ID_PLAYER, builder.build().apply {
            flags = flags.or(FLAG_ALWAYS_SHOW_TICKER)
        })
        callback?.onNotificationChanged(mediaNotification!!)
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) return

        if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID_PLAYER) == null) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID_PLAYER,
                    NOTIFICATION_CHANNEL_NAME_PLAYER,
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID_LOGGER) == null) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID_LOGGER,
                    NOTIFICATION_CHANNEL_NAME_LOGGER,
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    init {
        safeLaunch {
            lyricManager.currentSentence.combine(lyricPusherEnable) { text, enable ->
                if (enable) text else null
            }.collectLatest { text ->
                mediaNotification ?: return@collectLatest
                mediaNotification!!.notification.apply {
                    tickerText = text
                    builder.setTicker(text)
                    flags = flags.or(FLAG_ALWAYS_SHOW_TICKER)
                    flags = flags.or(FLAG_ONLY_UPDATE_TICKER)
                }
                ensureNotificationChannel()
                callback?.onNotificationChanged(mediaNotification!!)
            }
        }

        SpManager.listen(Config.KEY_SETTINGS_STATUS_LYRIC_ENABLE,
            SpManager.SpBoolListener(Config.DEFAULT_SETTINGS_STATUS_LYRIC_ENABLE) {
                lyricPusherEnable.tryEmit(it)
            })
    }
}