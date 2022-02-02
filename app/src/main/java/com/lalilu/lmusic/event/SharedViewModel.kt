package com.lalilu.lmusic.event

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/**
 *  为数据的全局同步而设置的 ViewModel ，即 mEvent，
 *  其生命周期属于 Application，在 Service 和 Activity 中都可以通过上下文获取到实例对象
 *
 */
@Singleton
class SharedViewModel @Inject constructor() : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    val nowBgPalette: MutableLiveData<Palette?> = MutableLiveData(null)
    val isAppbarLayoutExpand: MutableLiveData<Event<Boolean?>> = MutableLiveData(null)
    val isSearchViewExpand: MutableLiveData<Event<Boolean?>> = MutableLiveData(null)

    fun collapseAppbarLayout() = launch {
        delay(100)
        isAppbarLayoutExpand.postValue(Event(true))
    }

    fun collapseSearchView() {
        isSearchViewExpand.postValue(Event(true))
    }

}