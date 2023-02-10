package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.datastore.BlockedSp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DictionariesViewModel @Inject constructor(
    private val blockedSp: BlockedSp
) : ViewModel() {

    fun getBlockedPathsFlow(): Flow<List<String>> {
        return blockedSp.blockedPaths.flow()
            .map { it ?: emptyList() }
    }

    fun blockPath(path: String) {
        blockedSp.blockedPaths.add(path)
    }

    fun recoverPath(path: String) {
        blockedSp.blockedPaths.remove(path)
    }
}