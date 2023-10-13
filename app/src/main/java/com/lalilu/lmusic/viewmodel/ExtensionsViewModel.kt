package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.extension_core.ExtensionManager
import com.lalilu.lmusic.utils.extension.toState

class ExtensionsViewModel : ViewModel() {
    val extensionWithHomeContent = ExtensionManager
        .requireExtensionByContentKey(contentKey = "home")
        .toState(emptyList(), viewModelScope)
}