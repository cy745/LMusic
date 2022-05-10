package com.lalilu.lmusic.screen.viewmodel

import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.manager.LyricManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    lyricManager: LyricManager
) : ViewModel() {
    val songLyric = lyricManager.songLyric
}