package com.lalilu.lmusic.fragment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

/**
 * 为AlbumsFragment保存数据的ViewModel
 *
 */
class AlbumsViewModel @Inject constructor() : ViewModel() {
    val _position: MutableLiveData<Int?> = MutableLiveData()
    val position: LiveData<Int?> = _position
}