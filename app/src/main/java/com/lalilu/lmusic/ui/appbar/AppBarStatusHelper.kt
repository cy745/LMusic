package com.lalilu.lmusic.ui.appbar

import com.google.android.material.appbar.AppBarLayout
import de.halfbit.tinymachine.StateHandler
import de.halfbit.tinymachine.TinyMachine

/**
 * 监听AppBar的滚动和存储绘制部分的关键数据的全局单例
 */
const val STATE_COLLAPSED = 0
const val STATE_EXPENDED = 1
const val STATE_FULLY_EXPENDED = 2

const val EVENT_COLLAPSE = "EVENT_COLLAPSE"
const val EVENT_EXPEND = "EVENT_EXPEND"
const val EVENT_FULLY_COLLAPSE = "EVENT_FULLY_COLLAPSE"
const val EVENT_FULLY_EXPEND = "EVENT_FULLY_EXPEND"

object AppBarStatusHelper : AppBarLayout.OnOffsetChangedListener {
    private lateinit var appbar: AppBarLayout
    private var listenPercent: ((percent: Float) -> Unit)? = null
    val tinyMachine: TinyMachine = TinyMachine(this, STATE_EXPENDED)

    @Volatile
    var verticalOffset = 0
    var normalHeight = -1
    var maxExpandHeight = -1
    var maxDragHeight = 200
    var mBottom = -1

    /**
     * 根据下一个位置获取状态
     */
    fun getNextStatusByNextPosition(nextPosition: Int): Int {
        tinyMachine.transitionTo(
            when {
                tinyMachine.currentState == STATE_COLLAPSED &&
                        nextPosition >= appbar.height -> STATE_EXPENDED
                appbar.y + appbar.totalScrollRange < 0 || verticalOffset < 0 -> STATE_COLLAPSED
                else -> tinyMachine.currentState
            }
        )
        return tinyMachine.currentState
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        listenPercent?.invoke(1 + verticalOffset.toFloat() / appBarLayout.totalScrollRange)
        tinyMachine.transitionTo(
            when {
                appbar.y.toInt() == 0 && verticalOffset == 0 -> STATE_EXPENDED
                else -> tinyMachine.currentState
            }
        )
        this.verticalOffset = verticalOffset
    }

    /**
     * 初始化
     */
    fun initial(appbar: AppBarLayout, listenPercent: (percent: Float) -> Unit): AppBarStatusHelper {
        val fullyExpend = tinyMachine.currentState == STATE_FULLY_EXPENDED
        if (normalHeight <= 0) normalHeight = appbar.height
        if (mBottom <= 0 || !fullyExpend) mBottom = normalHeight

        this.appbar = appbar
        this.appbar.removeOnOffsetChangedListener(this)
        this.appbar.addOnOffsetChangedListener(this)
        this.listenPercent = listenPercent
        return this
    }

    @StateHandler(state = STATE_EXPENDED)
    fun onExpendedState(event: String, tm: TinyMachine) {
        println("onExpendedState: $event")
        when (event) {
            EVENT_EXPEND -> tm.transitionTo(STATE_FULLY_EXPENDED)
        }
    }

    @StateHandler(state = STATE_FULLY_EXPENDED)
    fun onFullyExpendedState(event: String, tm: TinyMachine) {
        println("onFullyExpendedState: $event")
        when (event) {
            EVENT_COLLAPSE -> tm.transitionTo(STATE_EXPENDED)
        }
    }

    @StateHandler(state = STATE_COLLAPSED, type = StateHandler.Type.OnEntry)
    fun onCollapsedEnter() {
        println("STATE_COLLAPSED")
    }

    @StateHandler(state = STATE_EXPENDED, type = StateHandler.Type.OnEntry)
    fun onExpendedEnter() {
        println("STATE_EXPENDED")
    }

    @StateHandler(state = STATE_FULLY_EXPENDED, type = StateHandler.Type.OnEntry)
    fun onFullyExpendedEnter() {
        println("STATE_FULLY_EXPENDED")
    }
}

