package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import com.lalilu.lmedia.entity.LDictionary
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.repository.LMediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DictionariesViewModel(
    private val lMusicSp: LMusicSp,
    private val lMediaRepo: LMediaRepository
) : ViewModel() {

    fun requireDictionary(dictionaryId: String): LDictionary? =
        lMediaRepo.requireDictionary(dictionaryId, false)

    fun getBlockedPathsFlow(): Flow<List<String>> {
        return lMusicSp.blockedPaths.flow()
            .map { it ?: emptyList() }
    }

    fun blockPath(path: String) {
        lMusicSp.blockedPaths.add(path)
    }

    fun recoverPath(path: String) {
        lMusicSp.blockedPaths.remove(path)
    }
}