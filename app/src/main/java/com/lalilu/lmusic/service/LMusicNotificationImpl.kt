package com.lalilu.lmusic.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datasource.MDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Singleton
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
    private var currentLyricTemp: String? = null

    /**
     *  API 26 以上需要注册Channel，否则不显示通知。
     */
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        notificationManager.cancelAll()
        launch {
            LMusicLyricManager.currentSentence.collectLatest {
                println("collectLatest: $it")

                currentLyricTemp = it
                updateNotification()
            }
        }
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
    override fun getService(): Service = mContext

    private var dataTemp: Any? = null
    private var lastBuilder: NotificationCompat.Builder? = null

    /**
     * 更新Notification
     *
     * @param data 传入给coil获取封面的对象，若为null则表示只需更新歌词
     */
    fun updateNotification(data: Any? = null) = launch {
        if (data != null) dataTemp = data

        withContext(Dispatchers.IO) {
            val builder = lastBuilder?.takeIf { data == null }
                ?: buildNotification(mContext.mediaSession)?.apply { lastBuilder = this }
                ?: return@withContext

            builder.loadCoverAndPalette(dataTemp)
            builder.setTicker(currentLyricTemp)

            println("[pushNotification]: updateNotification ${data == null}")
            pushNotification(builder.build().apply {
                flags = flags.or(FLAG_ALWAYS_SHOW_TICKER)

                // TODO 设置FLAG_ONLY_UPDATE_TICKER可能会导致notification不更新，不设置则会使状态栏歌词更新时出现两次歌词
                if (data == null) {
                    flags = flags.or(FLAG_ONLY_UPDATE_TICKER)
                }
            })
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