package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import com.lalilu.lmedia.entity.LSong
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor() : ViewModel() {


    /**
     * 请求获取每日推荐歌曲
     */
    fun requireDailyRecommends(): List<LSong> {
        return emptyList()
    }
}