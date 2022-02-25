package com.lalilu.lmusic.service.playback

import android.media.MediaPlayer
import com.lalilu.lmusic.manager.LMusicVolumeManager
import javax.inject.Inject

/**
 * 实现了播放暂停时音量渐变的MediaPlayer
 */
class LMusicPlayer @Inject constructor() : MediaPlayer() {
    private val volumeManager = LMusicVolumeManager(this)

    fun fadeStart() = volumeManager.fadeStart()
    fun fadePause() = volumeManager.fadePause()
}