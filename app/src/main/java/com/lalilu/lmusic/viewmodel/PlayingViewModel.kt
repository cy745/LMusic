package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmusic.manager.GlobalDataManager
import com.lalilu.lmusic.manager.LyricManager
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.utils.SeekBarHandler
import com.lalilu.lmusic.utils.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun onPlayPrevious() = viewModelScope.safeLaunch {
        mSongBrowser.browser?.seekToPrevious()
    }

    fun onPlayNext() = viewModelScope.safeLaunch {
        mSongBrowser.browser?.seekToNext()
    }

    fun onPlayPause() = viewModelScope.safeLaunch {
        mSongBrowser.togglePlay()
    }

    fun onSongSelected(mediaId: String) = viewModelScope.safeLaunch {
        mSongBrowser.playById(mediaId, true)
    }

    fun onSeekToPosition(position: Number) = viewModelScope.safeLaunch {
        mSongBrowser.browser?.seekTo(position.toLong())
    }

    fun onSongMoveToNext(mediaId: String): Boolean {
        return mSongBrowser.addToNext(mediaId)
    }

    fun onSongRemoved(mediaId: String): Boolean {
        return mSongBrowser.removeById(mediaId)
    }
}