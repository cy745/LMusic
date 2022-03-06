package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class PlayingViewModel @Inject constructor() : ViewModel() {
    val _lyric: MutableLiveData<String> = MutableLiveData()
    val _position: MutableLiveData<Long> = MutableLiveData()

    val lyric: LiveData<String> = _lyric
    val position: LiveData<Long> = _position
}