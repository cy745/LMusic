package com.lalilu.lmusic.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.domain.entity.PlaylistWithSongs

class PlayingFragmentViewModel : ViewModel() {
    val playlistWithSongs: MutableLiveData<PlaylistWithSongs> = MutableLiveData()
    val playingMSong: MutableLiveData<MSong> = MutableLiveData()
}