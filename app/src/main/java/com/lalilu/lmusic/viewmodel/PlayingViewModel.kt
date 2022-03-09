package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import javax.inject.Inject

class PlayingViewModel @Inject constructor() : BaseViewModel<List<MediaItem>>() {
    val song: MutableLiveData<MediaItem> = MutableLiveData()
}