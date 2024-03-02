package com.lalilu.lplayer.playback.impl

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * 为MediaPlayer添加isPrepared参数，方便判断是否已经prepare
 */
internal class LMediaPlayer : MediaPlayer {
    constructor() : super()

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    constructor(context: Context) : super(context)

    var isPrepared: Boolean = false
        private set

    private var listener: OnPreparedListener? = null
    private val listenerWrapper = OnPreparedListener {
        isPrepared = true
        listener?.onPrepared(it)
    }

    override fun setOnPreparedListener(listener: OnPreparedListener?) {
        this.listener = listener

        if (listener == null) {
            super.setOnPreparedListener(null)
        } else {
            super.setOnPreparedListener(listenerWrapper)
        }
    }

    override fun reset() {
        isPrepared = false
        super.reset()
    }

    override fun stop() {
        isPrepared = false
        super.stop()
    }
}