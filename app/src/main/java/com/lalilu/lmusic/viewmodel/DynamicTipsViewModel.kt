package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.utils.extension.toState

class DynamicTipsViewModel(
    lMusicSp: LMusicSp
) : ViewModel() {
    val enableState = lMusicSp.enableDynamicTips.flow()
        .toState(viewModelScope)
}