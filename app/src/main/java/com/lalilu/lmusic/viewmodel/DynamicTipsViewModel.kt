package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.utils.extension.toState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DynamicTipsViewModel @Inject constructor(
    lMusicSp: LMusicSp
) : ViewModel() {
    val enableState = lMusicSp.enableDynamicTips.flow()
        .toState(viewModelScope)
}