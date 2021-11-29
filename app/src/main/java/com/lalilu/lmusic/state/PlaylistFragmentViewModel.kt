package com.lalilu.lmusic.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.adapter.node.FirstNode
import com.lalilu.lmusic.domain.entity.MPlaylist

class PlaylistFragmentViewModel : ViewModel() {
    val playlist: MutableLiveData<List<FirstNode<MPlaylist>>> = MutableLiveData()
}