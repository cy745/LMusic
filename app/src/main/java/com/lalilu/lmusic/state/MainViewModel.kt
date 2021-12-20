package com.lalilu.lmusic.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import javax.inject.Inject

class MainViewModel @Inject constructor() : ViewModel() {
    val nowPageInt: MutableLiveData<Int> = MutableLiveData(0)
    val nowBgPalette: MutableLiveData<Palette> = MutableLiveData()
}