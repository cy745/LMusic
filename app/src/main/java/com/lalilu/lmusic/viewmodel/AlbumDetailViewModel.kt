package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import javax.inject.Inject

/**
 * 为AlbumDetailFragment保存数据的ViewModel
 *
 */
class AlbumDetailViewModel @Inject constructor() : ViewModel() {
    val _album: MutableLiveData<MediaItem?> = MutableLiveData()
    val album: LiveData<MediaItem?> = _album
}