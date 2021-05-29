package com.lalilu.lmusic.service2

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class MusicPlayer constructor(private val context: Context) {
    private lateinit var player: MediaPlayer
    private var prepared = false

    init {
        reCreate()
    }

    private fun reCreate() {
        player = MediaPlayer().also {
            it.setOnPreparedListener {
                prepared = true
                play()
            }
            it.setOnCompletionListener {

            }
        }
    }

    fun setDataSource(uri: Uri) {
        player.setDataSource(context, uri)
    }

    fun prepare() {
        player.prepareAsync()
    }

    fun play() {
        if (prepared) player.start()
    }

    fun pause() {
        if (player.isPlaying) player.pause()
    }

    fun stop() {
        player.reset()
    }

    fun seekTo(position: Long) {
        player.seekTo(position.toInt())
    }
}