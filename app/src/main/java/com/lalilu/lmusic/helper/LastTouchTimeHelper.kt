package com.lalilu.lmusic.helper

import android.view.MotionEvent
import com.lalilu.lmusic.LMusicFlowBus
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce

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

    @OptIn(FlowPreview::class)
    fun listenToLastTouchFlow(): Flow<Long> {
        return LMusicFlowBus.lastTouchTime.flow()
            .debounce(10000)
    }
}