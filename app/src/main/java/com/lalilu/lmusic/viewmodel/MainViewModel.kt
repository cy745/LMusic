package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.lalilu.lmusic.datasource.MMediaSource
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.utils.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mediaBrowser: MSongBrowser,
    val mediaSource: MMediaSource
) : ViewModel() {

    fun playSongWithPlaylist(items: List<MediaItem>, index: Int) =
        viewModelScope.safeLaunch {
            mediaBrowser.browser?.apply {
                clearMediaItems()
                setMediaItems(items)
                seekToDefaultPosition(index)
                prepare()
                play()
            }
        }
}