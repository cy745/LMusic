package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.datastore.LMusicSp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DictionariesViewModel @Inject constructor(
    private val lmusicSp: LMusicSp
) : ViewModel() {

    fun getBlockedPathsFlow(): Flow<List<String>> {
        return lmusicSp.blockedPaths.flow()
            .map { it ?: emptyList() }
    }

    fun blockPath(path: String) {
        lmusicSp.blockedPaths.add(path)
    }

    fun recoverPath(path: String) {
        lmusicSp.blockedPaths.remove(path)
    }
}