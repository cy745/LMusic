package com.lalilu.lmusic.utils

import androidx.activity.OnBackPressedCallback

/**
 * 用于解决特殊情况下，Compose的特殊嵌套结构使OnBackPressedCallback无法以目标顺序注册的问题
 */
class OnBackPressedHelper(
    var callback: () -> Unit = {}
) : OnBackPressedCallback(false) {
    override fun handleOnBackPressed() {
        callback.invoke()
    }
}