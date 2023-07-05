package com.lalilu.lmusic.ui


typealias OnProgressChangeListener = (progress: Float) -> Unit

open class AppbarProgressHelper {
    private val progressChangeListeners = hashSetOf<OnProgressChangeListener>()

    protected var zeroToMaxProgress: Float = -1f
    protected var zeroToMinProgress: Float = -1f
    protected var fullProgress: Float = -1f

    fun updateProgress(min: Int, max: Int, value: Int) {
        kotlin.runCatching {
            zeroToMaxProgress = normalize(value.toFloat(), 0f, max.toFloat())
            zeroToMinProgress = normalize(value.toFloat(), min.toFloat(), 0f)
            fullProgress = normalize(value.toFloat(), min.toFloat(), max.toFloat())
        }
    }

    fun addOnProgressChangeListener(listener: OnProgressChangeListener) {
        progressChangeListeners.add(listener)
    }

    private fun normalize(value: Float, min: Float, max: Float): Float {
        return ((value - min) / (max - min))
            .coerceIn(0f, 1f)
    }
}