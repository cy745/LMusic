package com.lalilu.component.viewmodel

import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import com.lalilu.common.base.Playable

abstract class IPlayingViewModel : ViewModel() {
    /**
     * 综合播放操作
     *
     * @param mediaId 目标歌曲的ID
     * @param mediaIds 歌曲ID列表
     * @param playOrPause 当前正在播放则暂停，暂停则开始播放
     * @param addToNext 是否在播放前将该歌曲移动到下一首播放的位置
     */
    abstract fun play(
        mediaId: String,
        mediaIds: List<String>? = null,
        playOrPause: Boolean = false,
        addToNext: Boolean = false,
    )

    abstract fun <T> isItemPlaying(item: T, getter: (Playable) -> T): Boolean
    abstract fun isItemPlaying(compare: (Playable) -> Boolean): Boolean

    abstract fun requireLyric(item: Playable, callback: (hasLyric: Boolean) -> Unit)
    abstract fun requireHasLyric(item: Playable): SnapshotStateMap<String, Boolean>
    abstract fun isFavourite(item: Playable): Boolean
}