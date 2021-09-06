package com.lalilu.lmusic.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette

class MainActivityViewModel : ViewModel() {
    val nowBgPalette: MutableLiveData<Palette> = MutableLiveData()
}