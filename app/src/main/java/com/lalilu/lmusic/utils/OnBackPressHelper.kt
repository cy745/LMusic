package com.lalilu.lmusic.utils

import androidx.activity.OnBackPressedCallback

class OnBackPressHelper(
    var callback: () -> Unit = {}
) : OnBackPressedCallback(false) {
    override fun handleOnBackPressed() {
        callback()
    }
}