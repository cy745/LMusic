package com.lalilu.lmusic.helper

import android.view.MotionEvent
import com.lalilu.lmusic.LMusicFlowBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest

/**
 * 　用于记录最后一次点击屏幕事件的Helper
 */
object LastTouchTimeHelper {

    fun onDispatchTouchEvent(ev: MotionEvent?) {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                LMusicFlowBus.lastTouchTime.post(-1L)
            }

            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP,
            -> {
                LMusicFlowBus.lastTouchTime.post(System.currentTimeMillis())
            }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun listenToLastTouch(scope: CoroutineScope, callback: (Long) -> Unit) {
        LMusicFlowBus.lastTouchTime.flow()
            .debounce(10000)
            .mapLatest { callback(it) }
            .launchIn(scope)
    }
}