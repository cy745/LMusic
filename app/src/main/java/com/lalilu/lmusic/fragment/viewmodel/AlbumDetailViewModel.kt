package com.lalilu.lmusic.fragment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.domain.entity.MAlbum
import javax.inject.Inject

/**
 * 为AlbumDetailFragment保存数据的ViewModel
 *
 */
class AlbumDetailViewModel @Inject constructor() : ViewModel() {
    val _album: MutableLiveData<MAlbum?> = MutableLiveData()
    val album: LiveData<MAlbum?> = _album
}