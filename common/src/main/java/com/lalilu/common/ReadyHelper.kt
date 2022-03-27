package com.lalilu.common

import androidx.annotation.IntDef

const val STATE_CREATED = 1
const val STATE_INITIALIZING = 2
const val STATE_INITIALIZED = 3
const val STATE_ERROR = 4

@IntDef(
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
)
@Retention(AnnotationRetention.SOURCE)
annotation class ReadyState

/**
 * 简单的状态机，根据[readyState]的状态决定当前任务的执行或延后与否
 */
open class ReadyHelper {
    private var readyCallback: (Boolean) -> Unit = {}

    @ReadyState
    var readyState: Int = STATE_CREATED
        set(value) {
            if (field == value) return
            when (value) {
                STATE_INITIALIZED,
                STATE_ERROR -> synchronized(readyCallback) {
                    field = value
                    readyCallback.invoke(value != STATE_ERROR)
                }
                else -> field = value
            }
        }

    fun whenReady(performAction: (Boolean) -> Unit): Boolean {
        return when (readyState) {
            STATE_CREATED, STATE_INITIALIZING -> {
                readyCallback = performAction
                false
            }
            else -> {
                performAction(readyState != STATE_ERROR)
                true
            }
        }
    }
}