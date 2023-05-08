package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import com.lalilu.lmedia.entity.LDictionary
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.repository.LMediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DictionariesViewModel(
    private val settingsSp: SettingsSp,
    private val lMediaRepo: LMediaRepository
) : ViewModel() {

    fun requireDictionary(dictionaryId: String): LDictionary? =
        lMediaRepo.requireDictionary(dictionaryId, false)

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