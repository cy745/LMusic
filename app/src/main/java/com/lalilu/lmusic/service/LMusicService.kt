package com.lalilu.lmusic.service

import android.content.Intent
import com.lalilu.lmedia.entity.HISTORY_TYPE_SONG
import com.lalilu.lmedia.entity.LHistory
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.repository.HistoryRepository
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.utils.EQHelper
import com.lalilu.lmusic.utils.extension.collectWithLifeCycleOwner
import com.lalilu.lplayer.LPlayer
import com.lalilu.lplayer.extensions.AudioFocusHelper
import com.lalilu.lplayer.playback.PlayMode
import com.lalilu.lplayer.service.LService
import org.koin.android.ext.android.inject

class LMusicService : LService() {
    private val intent: Intent by lazy { Intent(this@LMusicService, LMusicService::class.java) }
    override fun getStartIntent(): Intent = intent
    override fun getLoopDelay(isPlaying: Boolean): Long {
        return if (isPlaying) 50L else 0L
    }

    private val historyRepo: HistoryRepository by inject()
    private val settingsSp: SettingsSp by inject()
    private val eqHelper: EQHelper by inject()

    override fun onCreate() {
        super.onCreate()
        settingsSp.apply {
            volumeControl.flow(true)
                .collectWithLifeCycleOwner(this@LMusicService) {
                    it?.let { playback.setMaxVolume(it) }
                }
            enableSystemEq.flow(true)
                .collectWithLifeCycleOwner(this@LMusicService) {
                    eqHelper.setSystemEqEnable(it ?: false)
                }
            playMode.flow(true)
                .collectWithLifeCycleOwner(this@LMusicService) {
                    it?.let { playback.onSetPlayMode(PlayMode.of(it)) }
                }
            ignoreAudioFocus.flow(true)
                .collectWithLifeCycleOwner(this@LMusicService) {
                    AudioFocusHelper.ignoreAudioFocus = it ?: false
                }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val extras = intent?.extras
        when (intent?.action) {
            LPlayer.ACTION_SET_REPEAT_MODE -> {
                val playMode = extras?.getInt(PlayMode.KEY)?.takeIf { it in 0..2 }

                playMode?.let { settingsSp.playMode.set(it) }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var lastMediaId: String? = null
    private var startTime: Long = 0L
    private var duration: Long = 0L
    override fun onItemPlay(item: LSong) {
        val now = System.currentTimeMillis()
        if (startTime > 0) duration += now - startTime

        // 若切歌了或者播放时长超过阈值，更新或删除上一首歌的历史记录
        if (lastMediaId != item.id || duration >= Config.HISTORY_DURATION_THRESHOLD || duration >= item.durationMs) {
            if (lastMediaId != null) {
                if (duration >= Config.HISTORY_DURATION_THRESHOLD) {
                    historyRepo.updatePreSavedHistory(
                        contentId = lastMediaId!!,
                        duration = duration
                    )
                } else {
                    historyRepo.removePreSavedHistory(contentId = lastMediaId!!)
                }
            }

            // 将当前播放的歌曲预保存添加到历史记录中
            historyRepo.preSaveHistory(
                LHistory(
                    contentId = item.id,
                    duration = -1L,
                    startTime = now,
                    type = HISTORY_TYPE_SONG
                )
            )
            duration = 0L
        }

        startTime = now
        lastMediaId = item.id
    }

    override fun onItemPause(item: LSong) {
        // 判断当前暂停时的歌曲是否是最近正在播放的歌曲
        if (lastMediaId != item.id) return

        // 将该歌曲目前为止播放的时间加到历史记录中
        if (startTime > 0) {
            duration += System.currentTimeMillis() - startTime
            startTime = -1L
        }
    }

    override fun onPlayerCreated(id: Any) {
        if (id is Int) {
            eqHelper.audioSessionId = id
        }
    }
}