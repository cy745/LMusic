package com.lalilu.lmusic.utils

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import com.blankj.utilcode.util.IntentUtils.isIntentAvailable

object EQHelper {
    private var context: Context? = null
    private var enable: Boolean = false

    var audioSessionId: Int? = AudioEffect.ERROR_BAD_VALUE
        set(value) {
            field = value
            setSystemEqEnable(enable)
        }

    fun init(context: Context) {
        this.context = context
    }

    fun setSystemEqEnable(value: Boolean) {
        enable = value
        if (value) enableSystemEq() else closeSystemEq()
    }

    private fun enableSystemEq() {
        if (!checkAudioSessionId(audioSessionId) || !isSystemEqAvailable()) return

        Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context?.packageName)
            putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            context?.sendBroadcast(this)
        }
    }

    private fun closeSystemEq() {
        if (!checkAudioSessionId(audioSessionId) || !isSystemEqAvailable()) return

        Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context?.packageName)
            putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            context?.sendBroadcast(this)
        }
    }

    fun startSystemEqActivity(callback: (Intent) -> Unit) {
        if (!checkAudioSessionId(audioSessionId) || !isSystemEqAvailable()) return

        callback(Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context?.packageName)
            putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
        })
    }

    private fun isSystemEqAvailable(): Boolean =
        isIntentAvailable(Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL))

    private fun checkAudioSessionId(id: Int?): Boolean =
        id != null && id != AudioEffect.ERROR_BAD_VALUE
}