package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.repository.LyricRepository
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.service.runtime.LMusicRuntime
import com.lalilu.lmusic.utils.extension.toState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayingViewModel @Inject constructor(
    val runtime: LMusicRuntime,
    val browser: LMusicBrowser,
    val lyricRepository: LyricRepository
) : ViewModel() {
    private val playing = runtime.playingFlow.toState(viewModelScope)
    private val isPlaying = runtime.isPlayingFlow.toState(false, viewModelScope)

    fun playOrPauseSong(mediaId: String) {
        runtime.takeIf { it.getPlaying() != null && it._isPlayingFlow.value && it.getPlaying()?.id == mediaId }
            ?.let { browser.pause() } ?: browser.addAndPlay(mediaId)
    }

    fun playSongWithPlaylist(items: List<LSong>, item: LSong) = viewModelScope.launch {
        browser.setSongs(items, item)
        browser.reloadAndPlay()
    }

    fun isSongPlaying(mediaId: String): Boolean {
        if (!isPlaying.value) return false

        return playing.value?.let { it.id == mediaId } ?: false
    }
}

val LocalPlayingVM = compositionLocalOf<PlayingViewModel> {
    error("PlayingViewModel hasn't not presented")
}