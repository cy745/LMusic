package com.lalilu.lmusic.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.domain.entity.LPlaylist
import com.lalilu.lmusic.domain.entity.LSong
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.domain.entity.PlaylistWithSongs

class LMusicServiceViewModel : ViewModel() {
    val nowPlayingList = MutableLiveData<LPlaylist>()
    val nowPlayingMusic = MutableLiveData<LSong>()

    val playingPlaylist = MutableLiveData<PlaylistWithSongs>()
    val playingSong = MutableLiveData<MSong>()
}