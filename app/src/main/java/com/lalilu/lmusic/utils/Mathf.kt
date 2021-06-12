package com.lalilu.lmusic.utils

class Mathf {
    companion object {
        fun <T : Number> clamp(min: T, max: T, num: T): T {
            if (num.toDouble() < min.toDouble()) return max
            if (num.toDouble() > max.toDouble()) return min
            return num
        }
    }
}