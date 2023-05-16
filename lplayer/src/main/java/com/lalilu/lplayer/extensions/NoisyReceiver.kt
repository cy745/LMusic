package com.lalilu.lplayer.extensions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY

class NoisyReceiver constructor() : BroadcastReceiver() {
    var onBecomingNoisy: () -> Unit = {}
    private var mContext: Context? = null

    constructor(context: Context? = null) : this() {
        this.mContext = context
    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == ACTION_AUDIO_BECOMING_NOISY) {
            onBecomingNoisy()
        }
    }

    fun register() {
        mContext?.registerReceiver(
            this@NoisyReceiver,
            IntentFilter(ACTION_AUDIO_BECOMING_NOISY)
        )
    }

    fun unregister() {
        try {
            mContext?.unregisterReceiver(this@NoisyReceiver)
        } catch (e: Exception) {
            println("unregisterFrom: ${e.message}")
        }
    }
}