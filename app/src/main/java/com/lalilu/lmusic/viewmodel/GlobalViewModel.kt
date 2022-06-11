package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.manager.GlobalDataManager
import com.lalilu.lmusic.manager.LyricManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GlobalViewModel @Inject constructor(
    val globalDataManager: GlobalDataManager,
    val lyricManager: LyricManager
) : ViewModel()