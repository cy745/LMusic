package com.lalilu.lmusic

import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.utils.FlowBus

object LMusicFlowBus : FlowBus() {
    val libraryUpdate = longBus()
    val objEvent = objBus<LSong>()
}