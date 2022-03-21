package com.lalilu.lmusic.ui.seekbar

/**
 * 自定义的阈值判断辅助器
 */
class ThresholdHelper(var threshold: (value: Float) -> Boolean) {
    var handle: Boolean = false
        private set

    fun check(
        value: Float,
        callback: () -> Unit,
        recover: () -> Unit = {}
    ) {
        if (threshold(value) && !handle) {
            callback()
            handle = true
        }
        if (!threshold(value) && handle) {
            recover()
            handle = false
        }
    }

    fun reset() {
        handle = false
    }
}