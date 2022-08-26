package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmusic.manager.GlobalDataManager
import com.lalilu.lmusic.manager.LyricManager
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.utils.SeekBarHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayingViewModel @Inject constructor(
    val globalDataManager: GlobalDataManager,
    val lyricManager: LyricManager,
    val mSongBrowser: MSongBrowser
) : ViewModel() {
    val seekBarHandler = SeekBarHandler(
        this::onPlayNext,
        this::onPlayPrevious,
        this::onPlayPause
    )

    fun onPlayPrevious() = viewModelScope.launch {
        mSongBrowser.browser?.seekToPrevious()
    }

    fun onPlayNext() = viewModelScope.launch {
        mSongBrowser.browser?.seekToNext()
    }

    fun onPlayPause() = viewModelScope.launch {
        mSongBrowser.togglePlay()
    }

    fun onSongSelected(mediaId: String) = viewModelScope.launch {
        mSongBrowser.playById(mediaId, true)
    }

    fun onSeekToPosition(position: Number) = viewModelScope.launch {
        mSongBrowser.browser?.seekTo(position.toLong())
    }

    fun onSongMoveToNext(mediaId: String): Boolean {
        return mSongBrowser.addToNext(mediaId)
    }

    fun onSongRemoved(mediaId: String): Boolean {
        return mSongBrowser.removeById(mediaId)
    }
}