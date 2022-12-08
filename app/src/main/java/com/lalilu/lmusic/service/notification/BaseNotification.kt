package com.lalilu.lmusic.service.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.lalilu.R
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.service.LMusicService
import com.lalilu.lmusic.service.notification.LMusicNotifier.Companion.NOTIFICATION_PLAYER_ID
import com.lalilu.lmusic.utils.PlayMode

open class BaseNotification constructor(
    private val mContext: Context
) {
    fun buildMediaNotification(
        mediaSession: MediaSessionCompat,
        channelId: String
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

    protected val notificationManager: NotificationManager by lazy {
        ContextCompat.getSystemService(
            mContext, NotificationManager::class.java
        ) as NotificationManager
    }

    protected val mOrderPlayAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_order_play_line, "order_play",
        buildServicePendingIntent(
            mContext, 1,
            Intent(mContext, LMusicService::class.java)
                .setAction(Config.ACTION_SET_REPEAT_MODE)
                .putExtra(PlayMode.KEY, PlayMode.ListRecycle.next().value)
        )
    )
    protected val mSingleRepeatAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_repeat_one_line, "single_repeat",
        buildServicePendingIntent(
            mContext, 2,
            Intent(mContext, LMusicService::class.java)
                .setAction(Config.ACTION_SET_REPEAT_MODE)
                .putExtra(PlayMode.KEY, PlayMode.RepeatOne.next().value)
        )
    )
    protected val mShufflePlayAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_shuffle_line, "shuffle_play",
        buildServicePendingIntent(
            mContext, 3,
            Intent(mContext, LMusicService::class.java)
                .setAction(Config.ACTION_SET_REPEAT_MODE)
                .putExtra(PlayMode.KEY, PlayMode.Shuffle.next().value)
        )
    )
    protected val mPlayAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_play_line, "play",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            mContext, PlaybackStateCompat.ACTION_PLAY
        )
    )
    protected val mPauseAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_pause_line, "pause",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            mContext, PlaybackStateCompat.ACTION_PAUSE
        )
    )
    protected val mNextAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_skip_forward_line, "next",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            mContext, PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        )
    )
    protected val mPrevAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_skip_back_line, "previous",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            mContext, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )
    )
    protected val mStopAction: NotificationCompat.Action = NotificationCompat.Action(
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

    open fun pushNotification(notification: Notification) {
        notificationManager.notify(NOTIFICATION_PLAYER_ID, notification)
    }

    open fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}