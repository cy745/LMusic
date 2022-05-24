package com.lalilu.common

fun Float.ifNaN(default: Float = 0f, then: () -> Float = { default }): Float {
    return if (this.isNaN()) then() else this
}

fun calculatePercentIn(start: Number, end: Number, num: Number): Number {
    return ((num.toDouble() - start.toDouble()) / (end.toDouble() - start.toDouble()))
        .coerceIn(0.0, 1.0)
}