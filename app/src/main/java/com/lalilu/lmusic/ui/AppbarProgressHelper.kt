package com.lalilu.lmusic.ui


typealias OnProgressChangeListener = (progress: Float, fromUser: Boolean) -> Unit

open class AppbarProgressHelper {
    companion object {
        const val INVALID_PROGRESS = -1f
    }

    private val fullProgressChangeListeners = hashSetOf<OnProgressChangeListener>()
    private val zeroToMinProgressChangeListeners = hashSetOf<OnProgressChangeListener>()
    private val zeroToMaxProgressChangeListeners = hashSetOf<OnProgressChangeListener>()
    protected var actionFromUser: Boolean = false

    private var zeroToMaxProgress: Float = INVALID_PROGRESS
        set(value) {
            if (field == value) return
            field = value
            for (listener in zeroToMaxProgressChangeListeners) {
                listener.invoke(value, actionFromUser)
            }
        }
    private var zeroToMinProgress: Float = INVALID_PROGRESS
        set(value) {
            if (field == value) return
            field = value
            for (listener in zeroToMinProgressChangeListeners) {
                listener.invoke(value, actionFromUser)
            }
        }
    protected var fullProgress: Float = INVALID_PROGRESS
        set(value) {
            if (field == value) return
            field = value
            for (listener in fullProgressChangeListeners) {
                listener.invoke(value, actionFromUser)
            }
        }

    /**
     * 更新进度
     */
    fun updateProgress(min: Int, middle: Int, max: Int, value: Int) {
        kotlin.runCatching {
            zeroToMaxProgress = normalize(value.toFloat(), middle.toFloat(), max.toFloat())
            zeroToMinProgress = normalize(value.toFloat(), min.toFloat(), middle.toFloat())
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