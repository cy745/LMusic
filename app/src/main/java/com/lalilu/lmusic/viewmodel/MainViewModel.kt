package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.lalilu.lmusic.service.MSongBrowser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mediaBrowser: MSongBrowser
) : ViewModel() {

    fun playSongWithPlaylist(items: List<MediaItem>, index: Int) = viewModelScope.launch {
        mediaBrowser.browser?.apply {
            setMediaItems(items)
            seekToDefaultPosition(index)
            prepare()
            play()
        }
    }
}