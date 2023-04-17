package com.lalilu.ui.internal

import androidx.annotation.IntDef
import com.lalilu.ui.appbar.ExpendHeaderBehavior
import kotlinx.coroutines.flow.MutableStateFlow

class StateHelper {
    companion object {
        const val STATE_COLLAPSED = 0
        const val STATE_NORMAL = 1
        const val STATE_EXPENDED = 2
        const val STATE_MIDDLE = 3
        const val STATE_FULLY_EXPENDED = 4
    }

    /**
     * [ExpendHeaderBehavior]
     *  可能的几种状态
     */
    @IntDef(
        STATE_NORMAL,
        STATE_MIDDLE,
        STATE_COLLAPSED,
        STATE_EXPENDED,
        STATE_FULLY_EXPENDED
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class State

    /**
     * 监听状态变化的基础接口
     */
    fun interface OnStateChangeListener {
        fun onStateChange(@State lastState: Int, @State nowState: Int)
    }

    fun interface OnScrollToThresholdListener : OnStateChangeListener {
        fun onScrollToThreshold()
        override fun onStateChange(@State lastState: Int, @State nowState: Int) {
            if (lastState != STATE_MIDDLE && nowState == STATE_MIDDLE) {
                onScrollToThreshold()
            }
        }
    }

    abstract class OnScrollToStateListener(
        @State private val startState: Int,
        @State private val targetState: Int
    ) : OnStateChangeListener {
        abstract fun onScrollToStateListener()
        override fun onStateChange(@State lastState: Int, @State nowState: Int) {
            if (lastState == startState && nowState == targetState && nowState != lastState) {
                onScrollToStateListener()
            }
        }
    }

    private var stateChangeListeners = LinkedHashSet<OnStateChangeListener>()

    @State
    var lastState: Int = STATE_EXPENDED

    @State
    var nowState: Int = STATE_EXPENDED
        set(value) {
            if (field == value) return
            lastState = field
            stateChangeListeners.forEach { it.onStateChange(lastState, value) }
            field = value
            nowStateFlow.value = value
        }

    val nowStateFlow = MutableStateFlow(nowState)

    interface Adapter {
        val stateHelper: StateHelper

        fun addOnStateChangeListener(listener: OnStateChangeListener) {
            if (!stateHelper.stateChangeListeners.contains(listener)) {
                stateHelper.stateChangeListeners.add(listener)
            }
        }

        fun removeListener(listener: OnStateChangeListener) {
            stateHelper.stateChangeListeners.remove(listener)
        }

        fun removeAllListener() {
            stateHelper.stateChangeListeners.clear()
        }
    }
}