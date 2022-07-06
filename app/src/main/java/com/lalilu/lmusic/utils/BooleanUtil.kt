package com.lalilu.lmusic.utils

fun Boolean.then(onTrue: () -> Unit): Boolean {
    if (this) onTrue()
    return this
}

fun Boolean.or(onFalse: () -> Unit): Boolean {
    if (!this) onFalse()
    return this
}