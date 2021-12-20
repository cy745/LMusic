package com.lalilu.lmusic.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.domain.entity.PlaylistWithSongs
import javax.inject.Inject

class PlayingFragmentViewModel @Inject constructor() : ViewModel() {
    val playlistWithSongs: MutableLiveData<PlaylistWithSongs> = MutableLiveData()
    val playingMSong: MutableLiveData<MSong> = MutableLiveData()
}