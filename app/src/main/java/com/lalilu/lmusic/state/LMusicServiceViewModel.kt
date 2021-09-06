package com.lalilu.lmusic.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.domain.entity.LPlaylist
import com.lalilu.lmusic.domain.entity.LSong

class LMusicServiceViewModel : ViewModel() {
    val nowPlayingList = MutableLiveData<LPlaylist>()
    val nowPlayingMusic = MutableLiveData<LSong>()
}