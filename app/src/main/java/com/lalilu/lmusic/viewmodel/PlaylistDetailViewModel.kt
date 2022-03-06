package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.datasource.entity.MPlaylist
import javax.inject.Inject

/**
 * 为PlaylistDetailFragment保存数据的ViewModel
 *
 */
class PlaylistDetailViewModel @Inject constructor() : ViewModel() {
    val _playlist: MutableLiveData<MPlaylist?> = MutableLiveData()
    val playlist: LiveData<MPlaylist?> = _playlist
}