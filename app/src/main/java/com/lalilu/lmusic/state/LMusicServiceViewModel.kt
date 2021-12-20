package com.lalilu.lmusic.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.domain.entity.PlaylistWithSongs
import javax.inject.Inject

class LMusicServiceViewModel @Inject constructor() : ViewModel() {
    val playingPlaylist = MutableLiveData<PlaylistWithSongs>()
    val playingSong = MutableLiveData<MSong>()
}