package com.lalilu.lmusic.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.media.entity.Music

class LMusicServiceViewModel : ViewModel() {
    val nowPlayingList = MutableLiveData<List<Music>>()
    val nowPlayingMusic = MutableLiveData<Music>()
}