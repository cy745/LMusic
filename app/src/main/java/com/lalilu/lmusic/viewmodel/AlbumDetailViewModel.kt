package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import javax.inject.Inject

/**
 * 为AlbumDetailFragment保存数据的ViewModel
 *
 */
class AlbumDetailViewModel @Inject constructor() : BaseViewModel<List<MediaItem>>() {
    val album: MutableLiveData<MediaItem?> = MutableLiveData()
}