package com.lalilu.component.viewmodel

import androidx.lifecycle.ViewModel

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
}