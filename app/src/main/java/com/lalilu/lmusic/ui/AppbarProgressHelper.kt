package com.lalilu.lmusic.ui


typealias OnProgressChangeListener = (progress: Float) -> Unit

class AppbarProgressHelper(appbar: CoverAppbar) : AppbarStateHelper(appbar) {
    private val progressChangeListeners = hashSetOf<OnProgressChangeListener>()

    fun addOnProgressChangeListener(listener: OnProgressChangeListener) {
        progressChangeListeners.add(listener)
    }
}