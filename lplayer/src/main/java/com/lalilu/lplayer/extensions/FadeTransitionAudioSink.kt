package com.lalilu.lplayer.extensions

import android.content.Context
import androidx.annotation.OptIn
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.dynamicanimation.animation.withSpringForceProperties
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.ForwardingAudioSink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class FadeTransitionAudioSink(
    sink: AudioSink,
    val scope: CoroutineScope,
) : ForwardingAudioSink(sink) {
    private var volumeOverride = 0f
        set(value) {
            field = value
            super.setVolume((value / 100f).coerceIn(0f..1f))
        }

    private var onFinished: (() -> Unit)? = null
    private val animation = springAnimationOf(
        getter = { volumeOverride },
        setter = { volumeOverride = it },
    ).withSpringForceProperties {
        stiffness = SpringForce.STIFFNESS_LOW
        dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
    }.apply {
        setStartValue(0f)
        setStartVelocity(0f)
        addEndListener { _, canceled, _, _ ->
            if (!canceled) onFinished?.invoke()
        }
    }

    override fun setVolume(volume: Float) {
        volumeOverride = volume
    }

    override fun play() {
        scope.launch(Dispatchers.Main) { animation.animateToFinalPosition(100f) }
        onFinished = null
        super.play()
    }

    override fun pause() {
        scope.launch(Dispatchers.Main) { animation.animateToFinalPosition(0f) }
        onFinished = { super.pause() }
    }
}

@OptIn(UnstableApi::class)
class FadeTransitionRenderersFactory(
    context: Context,
    val scope: CoroutineScope,
) : DefaultRenderersFactory(context) {
    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean
    ): AudioSink? {
        return super.buildAudioSink(context, enableFloatOutput, enableAudioTrackPlaybackParams)
            ?.let { FadeTransitionAudioSink(it, scope) }
    }
}