package com.lalilu.lmusic.fragment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.domain.entity.MSong
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 为SongDetailFragment保存数据的ViewModel
 *
 * 必须设置[Singleton]注解，
 * 确保Fragment每次重置时都必须拿到相同的ViewModel，
 * 否则会导致先前绑定的数据在旋转之类的场景丢失
 */
@Singleton
class SongDetailViewModel @Inject constructor() : ViewModel() {
    val _song: MutableLiveData<MSong?> = MutableLiveData()
    val song: LiveData<MSong?> = _song
}