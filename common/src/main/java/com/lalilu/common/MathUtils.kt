package com.lalilu.common

fun Float.ifNaN(default: Float = 0f, then: () -> Float = { default }): Float {
    return if (this.isNaN()) then() else this
}
