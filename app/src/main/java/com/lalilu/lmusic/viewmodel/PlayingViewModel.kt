package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.repository.LyricRepository
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.service.playback.Playback
import com.lalilu.lmusic.service.runtime.LMusicRuntime
import com.lalilu.lmusic.utils.extension.toState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayingViewModel(
    val runtime: LMusicRuntime,
    val browser: LMusicBrowser,
    val lMusicSp: LMusicSp,
    val lyricRepository: LyricRepository
) : ViewModel() {
    private val playing = runtime.playingFlow.toState(viewModelScope)
    private val isPlaying = runtime.isPlayingFlow.toState(false, viewModelScope)

    val currentLyric = lyricRepository.currentLyric.asLiveData(viewModelScope.coroutineContext)
    val currentPosition = runtime.positionFlow.asLiveData(viewModelScope.coroutineContext)
    val currentPlaying = runtime.playingFlow.asLiveData(viewModelScope.coroutineContext)
    val currentSongs = runtime.songsFlow.asLiveData(viewModelScope.coroutineContext)

    fun playOrPauseSong(mediaId: String) {
        if (runtime.getPlayingId() == mediaId) {
            browser.sendCustomAction(Playback.PlaybackAction.PlayPause)
        } else {
            browser.addAndPlay(mediaId)
        }
    }

    fun playSongWithPlaylist(items: List<LSong>, item: LSong) = viewModelScope.launch {
        browser.setSongs(items)
        playOrPauseSong(item.id)
    }

    fun isSongPlaying(mediaId: String): Boolean {
        if (!isPlaying.value) return false
        return playing.value?.let { it.id == mediaId } ?: false
    }

    fun isAlbumPlaying(albumId: String): Boolean {
        if (!isPlaying.value) return false
        return playing.value?.let { it.album?.id == albumId } ?: false
    }

    fun isArtistPlaying(artistName: String): Boolean {
        if (!isPlaying.value) return false
        return playing.value?.let { song -> song.artists.any { it.name == artistName } } ?: false
    }

    fun requireLyric(item: LSong, callback: (hasLyric: Boolean) -> Unit) {
        viewModelScope.launch {
            if (isActive) {
                val hasLyric = lyricRepository.hasLyric(item)
                withContext(Dispatchers.Main) { callback(hasLyric) }
            }
        }
    }

    fun requireHasLyricState(item: LSong): MutableState<Boolean> {
        return mutableStateOf(false).also {
            viewModelScope.launch {
                if (isActive) {
                    it.value = lyricRepository.hasLyric(item)
                }
            }
        }
    }
}