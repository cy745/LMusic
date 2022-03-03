package com.lalilu.lmusic.fragment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import javax.inject.Inject

class SongDetailViewModel @Inject constructor() : ViewModel() {
    val _song: MutableLiveData<MediaItem?> = MutableLiveData()
    val song: LiveData<MediaItem?> = _song
}