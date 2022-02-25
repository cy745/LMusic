package com.lalilu.lmusic.fragment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.domain.entity.MSong
import javax.inject.Inject

class SongDetailViewModel @Inject constructor() : ViewModel() {
    val _song: MutableLiveData<MSong?> = MutableLiveData()
    val song: LiveData<MSong?> = _song
}