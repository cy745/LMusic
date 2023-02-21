package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.datastore.LMusicSp
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GuidingViewModel @Inject constructor(
    val lMusicSp: LMusicSp
) : ViewModel() {
}