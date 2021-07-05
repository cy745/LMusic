package com.lalilu.lmusic.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.adapter.node.FirstNode
import com.lalilu.media.entity.Playlist

class PlaylistFragmentViewModel : ViewModel() {
    val playlist: MutableLiveData<List<FirstNode<Playlist?>>> = MutableLiveData()
}