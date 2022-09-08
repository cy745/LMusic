package com.lalilu.lmusic.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.os.Build
import androidx.annotation.RequiresApi
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datasource.MDataBase
import com.lalilu.lmusic.utils.extension.findShowLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Singleton
import kotlin.concurrent.timerTask
import kotlin.coroutines.CoroutineContext

/**
 *
 */
@Singleton
class LMusicNotificationImpl constructor(
    private val mContext: LMusicService,
    private val database: MDataBase,
    private val playBack: LMusicPlayBack<LSong>
) : LMusicNotification(mContext), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    companion object {
        const val NOTIFICATION_PLAYER_ID = 7
        const val NOTIFICATION_LOGGER_ID = 8

        const val playerChannelName = "LMusic Player"
        const val loggerChannelName = "LMusic Logger"

        const val FLAG_ALWAYS_SHOW_TICKER = 0x1000000
        const val FLAG_ONLY_UPDATE_TICKER = 0x2000000
    }

    private val channels = listOf(playerChannelName, loggerChannelName)

    /**
     *  API 26 以上需要注册Channel，否则不显示通知。
     */
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        notificationManager.cancelAll()
        LMusicLyricManager.currentLyricEntry.launchIn(this)
    }

    override fun fillData(data: Any?): Any? {
        data ?: return null
        if (data !is LSong) return null
        return database.networkDataDao().getById(data.id)
            ?.requireCoverUri()
            ?: data
    }

    override fun getIsPlaying(): Boolean = playBack.getIsPlaying()
    override fun getIsStop(): Boolean = playBack.getIsStopped()
    override fun getPosition(): Long = playBack.getPosition()
    override fun getService(): Service = mContext

    private var lyricUpdateTimer: Timer? = null

    /**
     * 更新Notification
     *
     * @param data 传入给coil获取封面的对象，若为null则表示只需更新歌词
     */
    fun updateNotification(data: Any?) = launch(Dispatchers.IO) {
        // 无论任何操作都先将定时器取消
        lyricUpdateTimer?.cancel()
        lyricUpdateTimer = null

        val builder = buildNotification(mContext.mediaSession)
            ?.loadCoverAndPalette(data)
            ?: return@launch
        val notification = builder.build().apply {
            flags = flags.or(FLAG_ALWAYS_SHOW_TICKER)
            pushNotification(this)
        }

        // 只有处于播放中的状态时才推送状态栏歌词
        if (getIsPlaying()) {
            var lastLyricIndex = 0

            lyricUpdateTimer = Timer()
            lyricUpdateTimer?.schedule(timerTask {
                if (!getIsPlaying()) return@timerTask
                val lyricList = LMusicLyricManager.currentLyricEntry.get() ?: return@timerTask
                val time = getPosition().takeIf { it >= 0L } ?: return@timerTask
                val index = findShowLine(lyricList, time + 500)

                if (lastLyricIndex != index) {
                    val lyricEntry = lyricList.getOrNull(index)
                    val nowLyric = lyricEntry?.text ?: lyricEntry?.secondText

                    notification.apply {
                        if (nowLyric != null) {
                            tickerText = nowLyric
                            flags = flags.or(FLAG_ALWAYS_SHOW_TICKER)
                            flags = flags.or(FLAG_ONLY_UPDATE_TICKER)
                        }
                    }
                    pushNotification(notification)
                    lastLyricIndex = index
                }
            }, 200, 200)
        }
    }

    fun cancelNotification() {
        mContext.stopForeground(true)
        notificationManager.cancelAll()
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