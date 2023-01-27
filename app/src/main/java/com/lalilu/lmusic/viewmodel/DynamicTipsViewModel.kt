package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmusic.datastore.SettingsDataStore
import com.lalilu.lmusic.utils.extension.toState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DynamicTipsViewModel @Inject constructor(
    settingsDataStore: SettingsDataStore
) : ViewModel() {
    val enableState = settingsDataStore.run { this.enableDynamicTips.flow() }
        .toState(viewModelScope)
}