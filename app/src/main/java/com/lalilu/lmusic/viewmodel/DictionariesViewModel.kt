package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LDictionary
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.component.extension.toState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DictionariesViewModel(
    private val settingsSp: SettingsSp,
) : ViewModel() {
    val allDictionaries = LMedia.getFlow<LDictionary>(blockFilter = false)
        .toState(emptyList(), viewModelScope)

    fun requireDictionary(dictionaryId: String): LDictionary? =
        LMedia.get(dictionaryId, false)

    fun getBlockedPathsFlow(): Flow<List<String>> {
        return settingsSp.blockedPaths.flow()
            .map { it ?: emptyList() }
    }

    fun blockPath(path: String) {
        settingsSp.blockedPaths.add(path)
    }

    fun recoverPath(path: String) {
        settingsSp.blockedPaths.remove(path)
    }
}