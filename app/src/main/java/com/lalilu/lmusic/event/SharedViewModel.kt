package com.lalilu.lmusic.event

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import javax.inject.Inject
import javax.inject.Singleton

/**
 *  为数据的全局同步而设置的 ViewModel ，即 mEvent，
 *  其生命周期属于 Application，在 Service 和 Activity 中都可以通过上下文获取到实例对象
 *
 */
@Singleton
class SharedViewModel @Inject constructor() : ViewModel() {
    val nowBgPalette = MutableLiveData<Palette>()
}