package com.lalilu.lmusic.fragment.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.domain.entity.MSong
import javax.inject.Inject

class PlayingViewModel @Inject constructor() : ViewModel() {
    val _title: MutableLiveData<String> = MutableLiveData()
    val _mediaUri: MutableLiveData<Uri> = MutableLiveData()
    val _lyric: MutableLiveData<String> = MutableLiveData()
    val _position: MutableLiveData<Long> = MutableLiveData()
    val _songs: MutableLiveData<List<MSong>> = MutableLiveData()

    val title: LiveData<String> = _title
    val mediaUri: LiveData<Uri> = _mediaUri
    val lyric: LiveData<String> = _lyric
    val position: LiveData<Long> = _position
    val songs: LiveData<List<MSong>> = _songs
}