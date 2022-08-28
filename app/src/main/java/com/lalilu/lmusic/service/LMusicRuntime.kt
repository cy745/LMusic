package com.lalilu.lmusic.service

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import com.lalilu.lmedia.entity.LSong

/**
 * 全局单例，专门用于解决Service和Activity直接复杂的数据交互问题
 */
object LMusicRuntime {
    val currentPlaylist: MutableList<LSong> = mutableListOf()
    val currentPlaying: LSong? = null
    val currentPlayingIndex: Int = -1
    var currentIsPlaying: Boolean = false

    var currentIsPLayingState: MutableState<Boolean> = mutableStateOf(false)
    var currentPlayingState: MutableState<LSong?> = mutableStateOf(null)

    val currentPlayingLiveData: MutableLiveData<LSong> = MutableLiveData()
    val currentPlaylistLiveData: MutableLiveData<List<LSong>> = MutableLiveData(emptyList())

    fun updatePlaylist() {
        currentPlaylistLiveData.postValue(currentPlaylist)
    }

    fun updatePlaying() {
        currentPlayingLiveData.postValue(currentPlaying)
    }
}