package com.lalilu.lmusic

import com.lalilu.lmusic.utils.FlowBus

object LMusicFlowBus : FlowBus() {
    val lastTouchTime = longBus()
}