package com.lalilu.lmusic.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.os.Build
import androidx.annotation.RequiresApi
import com.dirror.lyricviewx.LyricEntry
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datasource.MDataBase
import com.lalilu.lmusic.repository.LyricRepository
import com.lalilu.lmusic.service.LMusicService
import com.lalilu.lmusic.service.playback.LMusicPlayBack
import com.lalilu.lmusic.utils.CoroutineSynchronizer
import com.lalilu.lmusic.utils.StatusBarLyricExt
import com.lalilu.lmusic.utils.extension.debounce
import com.lalilu.lmusic.utils.extension.findShowLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/**
 *
 */
@Singleton
class LMusicNotificationImpl constructor(
    private val mContext: LMusicService,
    private val database: MDataBase,
    private val playBack: LMusicPlayBack<LSong>,
    private val lyricRepository: LyricRepository
) : LMusicNotification(mContext), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    private val synchronizer = CoroutineSynchronizer()

    var statusLyricEnable = true
        set(value) {
            field = value
            buildNotification()?.let {
                if (value) startLyricPushCycle(it) else cancelLyricNotification(it)
            }
        }

    /**
     *  API 26 以上需要注册Channel，否则不显示通知。
     */
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        notificationManager.cancelAll()
        lyricRepository.currentLyricEntry.launchIn(this)
    }

    override fun fillData(data: Any?): Any? {
        data ?: return null
        if (data !is LSong) return null
        return database.networkDataDao().getById(data.id)
            ?.requireCoverUri()
            ?: data
    }

    override fun getImageData(): Any? = playBack.getCurrent()
    override fun getIsPlaying(): Boolean = playBack.getIsPlaying()
    override fun getIsStop(): Boolean = playBack.getIsStopped()
    override fun getPosition(): Long = playBack.getPosition()
    override fun getService(): Service = mContext

    private fun buildNotification(): Notification? {
        return buildNotification(mContext.mediaSession, PLAYER_CHANNEL_ID)
            ?.loadCoverAndPalette()
            ?.build()
    }

    /**
     * 取消Notification,关闭前台服务
     */
    fun cancel() {
        synchronizer.getCount()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mContext.stopForeground(Service.STOP_FOREGROUND_REMOVE)
        } else {
            mContext.stopForeground(true)
        }
        notificationManager.cancelAll()
    }

    /**
     * 更新Notification
     */
    fun update() = updateDebounceHelper.invoke()

    /**
     * 创建防抖
     */
    private val updateDebounceHelper = debounce(50) {
        val count = synchronizer.getCount()

        synchronizer.checkCount(count)
        val notification = buildNotification()?.apply {
            synchronizer.checkCount(count)
            flags = flags.or(FLAG_ALWAYS_SHOW_TICKER)
            pushNotification(this)
        } ?: return@debounce

        synchronizer.checkCount(count)
        startLyricPushCycle(notification)
    }

    /**
     * 启动推送状态栏歌词的循环
     */
    private fun startLyricPushCycle(notification: Notification) = launch {
        val count = synchronizer.getCount()
        var lastLyricIndex = 0
        var lyricList: List<LyricEntry>
        var position: Long
        var index: Int

        StatusBarLyricExt.stop()
        while (true) {
            delay(200)

            if (!getIsPlaying() || !statusLyricEnable) return@launch
            synchronizer.checkCount(count)

            position = getPosition().takeIf { it >= 0L } ?: continue
            lyricList = lyricRepository.currentLyricEntry.get() ?: continue
            index = findShowLine(lyricList, position + 500)
            if (lastLyricIndex == index) continue

            val nowLyric = lyricList.getOrNull(index)?.let {
                it.text.takeIf(String::isNotEmpty)
                    ?: it.secondText?.takeIf(String::isNotEmpty)
            }

            StatusBarLyricExt.send(nowLyric)
            notification.apply {
                tickerText = nowLyric
                flags = flags.or(FLAG_ONLY_UPDATE_TICKER)
                flags = flags.or(FLAG_ALWAYS_SHOW_TICKER)
                pushNotification(this)
            }
            lastLyricIndex = index
        }
    }

    /**
     * 取消状态栏歌词，不影响媒体控制器
     */
    private fun cancelLyricNotification(notification: Notification) = launch {
        synchronizer.getCount()
        StatusBarLyricExt.stop()

        notification.apply {
            tickerText = null
            flags = flags.or(FLAG_ONLY_UPDATE_TICKER)
            flags = flags.or(FLAG_ALWAYS_SHOW_TICKER)
            pushNotification(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        NotificationChannel(
            PLAYER_CHANNEL_ID,
            PLAYER_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "【LMusic通知频道】：$PLAYER_CHANNEL_NAME"
            importance = NotificationManager.IMPORTANCE_LOW
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
            notificationManager.createNotificationChannel(this)
        }
    }

    companion object {
        const val NOTIFICATION_PLAYER_ID = 7
        const val NOTIFICATION_LOGGER_ID = 8

        private const val PLAYER_CHANNEL_NAME = "LMusic Player"
        private const val LOGGER_CHANNEL_NAME = "LMusic Logger"

        const val PLAYER_CHANNEL_ID = PLAYER_CHANNEL_NAME + "_ID"
        const val LOGGER_CHANNEL_ID = PLAYER_CHANNEL_NAME + "_ID"

        const val FLAG_ALWAYS_SHOW_TICKER = 0x1000000
        const val FLAG_ONLY_UPDATE_TICKER = 0x2000000
    }
}