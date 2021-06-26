package com.lalilu.player.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY

class MusicNoisyReceiver : BroadcastReceiver() {
    var onBecomingNoisyListener: OnBecomingNoisyListener? = null

    interface OnBecomingNoisyListener {
        fun onBecomingNoisy()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_AUDIO_BECOMING_NOISY) {
            onBecomingNoisyListener?.onBecomingNoisy()
        }
    }
}