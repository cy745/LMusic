package com.lalilu.lmusic.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.lalilu.lmusic.domain.entity.LPlaylist
import com.lalilu.lmusic.domain.entity.LSong
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.domain.entity.PlaylistWithSongs

class PlayingFragmentViewModel : ViewModel() {
    val nowBgPalette: MutableLiveData<Palette> = MutableLiveData()

    @Deprecated("playlistWithSongs 替代")
    val musicList: MutableLiveData<LPlaylist> = MutableLiveData()

    @Deprecated("playingMSong 替代")
    val nowPlayingMusic: MutableLiveData<LSong> = MutableLiveData()

    val playlistWithSongs: MutableLiveData<PlaylistWithSongs> = MutableLiveData()
    val playingMSong: MutableLiveData<MSong> = MutableLiveData()
}