package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.common.base.Playable
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.repository.LyricRepository
import com.lalilu.lmusic.utils.extension.toState
import com.lalilu.lplayer.LPlayer
import com.lalilu.lplayer.extensions.PlayerAction
import com.lalilu.lplayer.extensions.QueueAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayingViewModel(
    val settingsSp: SettingsSp,
    val lyricRepository: LyricRepository,
) : ViewModel() {
    val playing = LPlayer.runtime.info.playingFlow.toState(viewModelScope)
    val isPlaying = LPlayer.runtime.info.isPlayingFlow.toState(false, viewModelScope)

    /**
     * 综合播放操作
     *
     * @param mediaId 目标歌曲的ID
     * @param mediaIds 歌曲ID列表
     * @param playOrPause 当前正在播放则暂停，暂停则开始播放
     * @param addToNext 是否在播放前将该歌曲移动到下一首播放的位置
     */
    fun play(
        mediaId: String,
        mediaIds: List<String>? = null,
        playOrPause: Boolean = false,
        addToNext: Boolean = false,
    ) = viewModelScope.launch {
        if (mediaIds != null) {
            QueueAction.UpdateList(mediaIds).action()
        }
        if (addToNext) {
            QueueAction.AddToNext(mediaId).action()
        }
        if (mediaId == LPlayer.runtime.queue.getCurrentId() && playOrPause) {
            PlayerAction.PlayOrPause.action()
        } else {
            PlayerAction.PlayById(mediaId).action()
        }
    }

    fun <T> isItemPlaying(item: T, getter: (Playable) -> T): Boolean =
        isItemPlaying { item == getter(it) }

    fun isItemPlaying(compare: (Playable) -> Boolean): Boolean {
        if (!isPlaying.value) return false
        return playing.value?.let { compare(it) } ?: false
    }

    fun requireLyric(item: Playable, callback: (hasLyric: Boolean) -> Unit) {
        viewModelScope.launch {
            if (isActive) {
                val hasLyric = lyricRepository.hasLyric(item)
                withContext(Dispatchers.Main) { callback(hasLyric) }
            }
        }
    }

    private val hasLyricList = mutableStateMapOf<String, Boolean>()
    fun requireHasLyric(item: Playable): SnapshotStateMap<String, Boolean> {
        viewModelScope.launch {
            if (!isActive) return@launch
            hasLyricList[item.mediaId] = lyricRepository.hasLyric(item)
        }
        return hasLyricList
    }
}