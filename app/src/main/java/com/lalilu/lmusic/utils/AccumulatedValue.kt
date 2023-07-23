package com.lalilu.lmusic.utils


/**
 * 用于动画值传递时进行强制类型转换时的数值修正
 */
class AccumulatedValue {
    private var accumulated: Float = 0f

    fun accumulate(value: Float): Int {
        accumulated += value
        val intValue = accumulated.toInt()
        accumulated -= intValue
        return intValue
    }

    /**
     * 提前获取累加后的数据值
     */
    fun ifAccumulate(value: Float): Int {
        return (accumulated + value).toInt()
    }
}