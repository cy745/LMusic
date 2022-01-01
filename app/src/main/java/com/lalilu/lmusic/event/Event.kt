package com.lalilu.lmusic.event

class Event<T> constructor(val value: T? = null) {
    private var _isSolved = false

    fun get(func: (T?) -> Unit) {
        if (!_isSolved) {
            _isSolved = true
            func(value)
        }
    }
}