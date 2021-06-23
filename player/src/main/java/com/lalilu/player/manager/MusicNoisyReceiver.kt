package com.lalilu.player.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY

class MusicNoisyReceiver : BroadcastReceiver() {
    lateinit var onBecomingNoisyCallback: () -> Unit

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_AUDIO_BECOMING_NOISY) {
            onBecomingNoisyCallback()
        }
    }
}