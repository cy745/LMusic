package com.lalilu.lmusic.service.playback

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY

class LMusicNoisyReceiver(val onBecomingNoisy: () -> Unit = {}) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == ACTION_AUDIO_BECOMING_NOISY) {
            onBecomingNoisy()
        }
    }

    fun registerTo(service: Service) {
        service.registerReceiver(
            this@LMusicNoisyReceiver,
            IntentFilter(ACTION_AUDIO_BECOMING_NOISY)
        )
    }

    fun unRegisterFrom(service: Service) {
        try {
            service.unregisterReceiver(this@LMusicNoisyReceiver)
        } catch (e: Exception) {
            println("unRegisterFrom: ${e.message}")
        }
    }
}