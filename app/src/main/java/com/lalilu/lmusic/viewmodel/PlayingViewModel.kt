package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.repository.LyricHelper
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.service.runtime.LMusicRuntime
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayingViewModel @Inject constructor(
    val runtime: LMusicRuntime,
    val browser: LMusicBrowser,
    val lyricHelper: LyricHelper
) : ViewModel() {
}