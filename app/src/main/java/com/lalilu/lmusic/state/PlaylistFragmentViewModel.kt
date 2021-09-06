package com.lalilu.lmusic.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.adapter.node.FirstNode
import com.lalilu.lmusic.domain.entity.LPlaylist

class PlaylistFragmentViewModel : ViewModel() {
    val playlist: MutableLiveData<List<FirstNode<LPlaylist?>>> = MutableLiveData()
}