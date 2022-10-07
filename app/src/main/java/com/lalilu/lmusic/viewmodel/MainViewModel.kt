package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.utils.SelectHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val isSelecting = mutableStateOf(false)
    val songSelectHelper = SelectHelper<LSong>(isSelecting)
    val playlistSelectHelper = SelectHelper<LPlaylist>(isSelecting)

    fun playSongWithPlaylist(items: List<LSong>, item: LSong) = viewModelScope.launch {
        LMusicBrowser.setSongs(items, item)
        LMusicBrowser.reloadAndPlay()
    }
}