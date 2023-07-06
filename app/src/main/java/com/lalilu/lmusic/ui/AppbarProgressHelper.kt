package com.lalilu.lmusic.ui


typealias OnProgressChangeListener = (progress: Float) -> Unit

open class AppbarProgressHelper {
    private val fullProgressChangeListeners = hashSetOf<OnProgressChangeListener>()
    private val zeroToMinProgressChangeListeners = hashSetOf<OnProgressChangeListener>()
    private val zeroToMaxProgressChangeListeners = hashSetOf<OnProgressChangeListener>()

    private var zeroToMaxProgress: Float = -1f
        set(value) {
            if (field == value) return
            field = value
            zeroToMaxProgressChangeListeners.forEach { it.invoke(field) }
        }
    private var zeroToMinProgress: Float = -1f
        set(value) {
            if (field == value) return
            field = value
            zeroToMinProgressChangeListeners.forEach { it.invoke(field) }
        }
    protected var fullProgress: Float = -1f
        set(value) {
            if (field == value) return
            field = value
            fullProgressChangeListeners.forEach { it.invoke(field) }
        }

    fun updateProgress(min: Int, max: Int, value: Int) {
        kotlin.runCatching {
            zeroToMaxProgress = normalize(value.toFloat(), 0f, max.toFloat())
            zeroToMinProgress = normalize(value.toFloat(), min.toFloat(), 0f)
            fullProgress = normalize(value.toFloat(), min.toFloat(), max.toFloat())
        }
    }

    fun addListenerForFullProgress(listener: OnProgressChangeListener) {
        fullProgressChangeListeners.add(listener)
    }

    fun addListenerForToMinProgress(listener: OnProgressChangeListener) {
        zeroToMinProgressChangeListeners.add(listener)
    }

    fun addListenerForToMaxProgress(listener: OnProgressChangeListener) {
        zeroToMaxProgressChangeListeners.add(listener)
    }

    private fun normalize(value: Float, min: Float, max: Float): Float {
        return ((value - min) / (max - min))
            .coerceIn(0f, 1f)
    }
}