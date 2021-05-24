package com.lalilu.lmusic

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.VolumeShaper
import android.os.Build
import com.lalilu.lmusic.entity.Song

class MusicPlayer(private val context: Context) {
    private lateinit var player: MediaPlayer
    private var prepared: Boolean = false
    private var shaper: VolumeShaper? = null
    private var config: VolumeShaper.Configuration? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            config = VolumeShaper.Configuration.Builder().setDuration(200)
                .setInterpolatorType(VolumeShaper.Configuration.INTERPOLATOR_TYPE_LINEAR)
                .setCurve(floatArrayOf(0f, 1f), floatArrayOf(0f, 1f))
                .build()
        }
        reCreate()
    }

    fun setOnCompletionListener(listener: MediaPlayer.OnCompletionListener) {
        player.setOnCompletionListener(listener)
    }

    fun setSong(song: Song) {
        player.reset()
        player.setDataSource(context, song.songUri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && config != null) {
            shaper = player.createVolumeShaper(config!!)
        }
        player.setOnPreparedListener {
            prepared = true
            play()
        }
        player.prepareAsync()
    }

    private fun reCreate() {
        player = MediaPlayer().also {
            it.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .build()
            )
            it.setOnPreparedListener {
                prepared = true
                play()
            }
        }
    }

    fun toggle() {
        if (player.isPlaying) pause() else play()
    }

    fun play() {
        if (prepared) {
            player.start()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                shaper?.apply(VolumeShaper.Operation.PLAY)
            }
        }
    }

    fun pause() {
        if (player.isPlaying) {
            player.pause()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                shaper?.apply(VolumeShaper.Operation.REVERSE)
            }
        }
    }

    fun stop() {
        if (player.isPlaying) {
            player.pause()
        }
    }

    fun setDuration(duration: Number) {
        if (prepared || player.isPlaying) {
            player.seekTo(duration.toInt())
        }
    }

    fun getDuration(): Int {
        return player.currentPosition
    }
}