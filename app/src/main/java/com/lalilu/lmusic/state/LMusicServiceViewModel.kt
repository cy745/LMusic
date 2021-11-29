package com.lalilu.lmusic.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.domain.entity.PlaylistWithSongs

class LMusicServiceViewModel : ViewModel() {
    val playingPlaylist = MutableLiveData<PlaylistWithSongs>()
    val playingSong = MutableLiveData<MSong>()
}