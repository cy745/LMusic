package com.lalilu.ui.appbar


typealias OnProgressChangeListener = (progress: Float, fromUser: Boolean) -> Unit

open class AppbarProgressHelper {
    companion object {
        const val INVALID_PROGRESS = -1f
        const val INVALID_POSITION = Int.MIN_VALUE
    }

    private val fullProgressChangeListeners = hashSetOf<OnProgressChangeListener>()
    private val zeroToMinProgressChangeListeners = hashSetOf<OnProgressChangeListener>()
    private val zeroToMaxProgressChangeListeners = hashSetOf<OnProgressChangeListener>()
    protected var actionFromUser: Boolean = false

    var zeroToMaxProgress: Float = INVALID_PROGRESS
        private set(value) {
            if (field == value) return
            field = value
            for (listener in zeroToMaxProgressChangeListeners) {
                listener.invoke(value, actionFromUser)
            }
        }
    var zeroToMinProgress: Float = INVALID_PROGRESS
        private set(value) {
            if (field == value) return
            field = value
            for (listener in zeroToMinProgressChangeListeners) {
                listener.invoke(value, actionFromUser)
            }
        }
    var fullProgress: Float = INVALID_PROGRESS
        private set(value) {
            if (field == value) return
            field = value
            for (listener in fullProgressChangeListeners) {
                listener.invoke(value, actionFromUser)
            }
        }

    /**
     * 更新进度
     */
    fun updateProgressByPosition(min: Int, middle: Int, max: Int, value: Int) {
        kotlin.runCatching {
            updateProgress(
                min = normalize(value.toFloat(), min.toFloat(), middle.toFloat()),
                max = normalize(value.toFloat(), middle.toFloat(), max.toFloat()),
                full = normalize(value.toFloat(), min.toFloat(), max.toFloat()),
            )
        }
    }

    /**
     * 更新进度
     */
    fun updateProgress(min: Float, max: Float, full: Float) {
        kotlin.runCatching {
            zeroToMaxProgress = max
            zeroToMinProgress = min
            fullProgress = full
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