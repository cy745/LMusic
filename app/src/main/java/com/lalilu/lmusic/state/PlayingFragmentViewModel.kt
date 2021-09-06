package com.lalilu.lmusic.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.domain.entity.LPlaylist

class PlayingFragmentViewModel : ViewModel() {
    val musicList: MutableLiveData<LPlaylist> = MutableLiveData()
}