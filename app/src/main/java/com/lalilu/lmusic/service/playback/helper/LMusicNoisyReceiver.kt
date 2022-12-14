package com.lalilu.lmusic.service.playback.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LMusicNoisyReceiver constructor() : BroadcastReceiver() {
    var onBecomingNoisy: () -> Unit = {}
    private var mContext: Context? = null

    @Inject
    constructor(
        @ApplicationContext context: Context? = null
    ) : this() {
        this.mContext = context
    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == ACTION_AUDIO_BECOMING_NOISY) {
            onBecomingNoisy()
        }
    }

    fun register() {
        mContext?.registerReceiver(
            this@LMusicNoisyReceiver,
            IntentFilter(ACTION_AUDIO_BECOMING_NOISY)
        )
    }

    fun unregister() {
        try {
            mContext?.unregisterReceiver(this@LMusicNoisyReceiver)
        } catch (e: Exception) {
            println("unregisterFrom: ${e.message}")
        }
    }
}