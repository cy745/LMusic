package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import com.lalilu.lmusic.datasource.entity.MPlaylist
import javax.inject.Inject

/**
 * 为PlaylistDetailFragment保存数据的ViewModel
 *
 */
class PlaylistDetailViewModel @Inject constructor() : BaseViewModel<List<MediaItem>>() {
    val playlist: MutableLiveData<MPlaylist?> = MutableLiveData()
}