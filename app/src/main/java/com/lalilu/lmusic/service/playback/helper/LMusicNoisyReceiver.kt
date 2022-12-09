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
class LMusicNoisyReceiver @Inject constructor(
    @ApplicationContext private val mContext: Context
) : BroadcastReceiver() {
    var onBecomingNoisy: () -> Unit = {}

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == ACTION_AUDIO_BECOMING_NOISY) {
            onBecomingNoisy()
        }
    }

    fun register() {
        mContext.registerReceiver(
            this@LMusicNoisyReceiver,
            IntentFilter(ACTION_AUDIO_BECOMING_NOISY)
        )
    }

    fun unregister() {
        try {
            mContext.unregisterReceiver(this@LMusicNoisyReceiver)
        } catch (e: Exception) {
            println("unregisterFrom: ${e.message}")
        }
    }
}