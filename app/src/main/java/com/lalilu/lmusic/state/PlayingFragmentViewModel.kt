package com.lalilu.lmusic.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.media.entity.Music

class PlayingFragmentViewModel : ViewModel() {
    val musicList: MutableLiveData<List<Music>> = MutableLiveData()
}