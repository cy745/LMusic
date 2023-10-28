package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class TempViewModel : ViewModel() {
    val isFastJumping = mutableStateOf(false)
    val isSelecting = mutableStateOf(false)
    val selectedItems = mutableStateOf<List<Any>>(emptyList())
    val showSortPanel = mutableStateOf(false)

}