package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import javax.inject.Inject

class SongDetailViewModel @Inject constructor() : ViewModel() {
    val song: MutableLiveData<MediaItem?> = MutableLiveData()
}