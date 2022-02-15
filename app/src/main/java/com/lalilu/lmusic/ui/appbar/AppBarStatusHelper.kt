package com.lalilu.lmusic.ui.appbar

import com.google.android.material.appbar.AppBarLayout
import de.halfbit.tinymachine.StateHandler
import de.halfbit.tinymachine.TinyMachine
import javax.inject.Inject
import javax.inject.Singleton

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

@Singleton
@Deprecated("弃用")
class AppBarStatusHelper @Inject constructor() : AppBarLayout.OnOffsetChangedListener {
    private lateinit var appbar: AppBarLayout
    private var listenPercent: ((percent: Float) -> Unit)? = null
    val tinyMachine: TinyMachine = TinyMachine(this, STATE_EXPENDED)
    val currentState get() = tinyMachine.currentState

    private var verticalOffset = 0
    var lastHeight = -1
    var normalHeight = -1
    var deviceHeight = -1
    var maxExpandHeight = -1
    var maxDragHeight = 200

    private var stateTransitionMap = HashMap<Int, (Int) -> Int?>().apply {
        put(STATE_COLLAPSED) {
            if (it >= appbar.height || it == 0) STATE_EXPENDED else null
        }
        put(STATE_EXPENDED) {
            if (appbar.y + appbar.totalScrollRange < 0 || verticalOffset < 0) STATE_COLLAPSED
            else null
        }
    }

    /**
     * 根据下一个位置获取状态
     */
    fun getNextStatusByNextPosition(nextPosition: Int): Int {
        stateTransitionMap[currentState]?.invoke(nextPosition)
            ?.let { tinyMachine.transitionTo(it) }
        return currentState
    }

    /**
     * Appbar的OffsetChange，用于判断Expended和Collapsed之间的状态变换
     */
    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        listenPercent?.invoke(1 + verticalOffset.toFloat() / appBarLayout.totalScrollRange)
        this.verticalOffset = verticalOffset

        stateTransitionMap[currentState]?.invoke(verticalOffset)
            ?.let { tinyMachine.transitionTo(it) }
    }

    /**
     * 初始化
     */
    fun initialize(
        appbar: AppBarLayout,
        listenPercent: (percent: Float) -> Unit
    ) {
        if (normalHeight <= 0) normalHeight = appbar.height

        this.appbar = appbar
        this.appbar.removeOnOffsetChangedListener(this)
        this.appbar.addOnOffsetChangedListener(this)
        this.listenPercent = listenPercent
    }

    @StateHandler(state = STATE_EXPENDED)
    fun onExpendedState(event: String, tm: TinyMachine) {
        when (event) {
            EVENT_EXPEND -> tm.transitionTo(STATE_FULLY_EXPENDED)
        }
    }

    @StateHandler(state = STATE_FULLY_EXPENDED)
    fun onFullyExpendedState(event: String, tm: TinyMachine) {
        when (event) {
            EVENT_COLLAPSE -> tm.transitionTo(STATE_EXPENDED)
        }
    }

//    @StateHandler(state = STATE_COLLAPSED, type = StateHandler.Type.OnEntry)
//    fun onCollapsedEnter() {
//        println("STATE_COLLAPSED")
//    }
//
//    @StateHandler(state = STATE_EXPENDED, type = StateHandler.Type.OnEntry)
//    fun onExpendedEnter() {
//        println("STATE_EXPENDED")
//    }
//
//    @StateHandler(state = STATE_FULLY_EXPENDED, type = StateHandler.Type.OnEntry)
//    fun onFullyExpendedEnter() {
//        println("STATE_FULLY_EXPENDED")
//    }
}

