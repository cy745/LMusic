package com.lalilu.lmusic.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.lalilu.lmusic.domain.entity.LPlaylist
import com.lalilu.lmusic.domain.entity.LSong

class PlayingFragmentViewModel : ViewModel() {
    val musicList: MutableLiveData<LPlaylist> = MutableLiveData()
    val nowBgPalette: MutableLiveData<Palette> = MutableLiveData()
    val nowPlayingMusic: MutableLiveData<LSong> = MutableLiveData()
}