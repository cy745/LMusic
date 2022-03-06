package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.MutableLiveData
import com.lalilu.lmusic.datasource.entity.MPlaylist
import javax.inject.Inject

/**
 * 为PlaylistsFragment保存数据的ViewModel
 *
 */
class AddToPlaylistViewModel @Inject constructor() : BaseViewModel<List<MPlaylist>>() {
    val title = MutableLiveData<String?>()
}