package com.lalilu.lmusic.service

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import com.lalilu.lmedia.entity.LSong

/**
 * 全局单例，专门用于解决Service和Activity之间复杂的数据交互问题
 */
object LMusicRuntime {
    val currentPlaylist: MutableList<LSong> = mutableListOf()
    var currentPlaying: LSong? = null
        set(value) {
            field = value
            updatePlaying()
        }
    var currentRepeatMode: Int = 0
    var currentShuffleMode: Int = 0

    val currentPlayingLiveData: MutableLiveData<LSong> = MutableLiveData()
    val currentPlaylistLiveData: MutableLiveData<List<LSong>> = MutableLiveData(emptyList())
    var currentIsPLayingState: MutableState<Boolean> = mutableStateOf(false)
    var currentPlayingState: MutableState<LSong?> = mutableStateOf(null)
    var currentIsPlaying: Boolean = false

    fun updatePlaylist() {
        currentPlaylistLiveData.postValue(currentPlaylist)
    }

    fun updatePlaying() {
        currentPlayingLiveData.postValue(currentPlaying)
    }
}