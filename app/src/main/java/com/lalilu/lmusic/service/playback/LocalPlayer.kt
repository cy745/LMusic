package com.lalilu.lmusic.service.playback

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) : Player, Player.Listener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private var volumeProxy: FadeVolumeProxy? = null
    private var player: MediaPlayer? = null
        get() {
            field = field ?: MediaPlayer().apply {
                setOnPreparedListener(this@LocalPlayer)
                setOnCompletionListener(this@LocalPlayer)
                volumeProxy = FadeVolumeProxy(this)
            }
            return field
        }

    override var listener: Player.Listener? = null
    override var isPlaying: Boolean = false
    override var isPrepared: Boolean = false
    override var isStopped: Boolean = true
    private var startWhenReady: Boolean = false


    override fun load(uri: Uri, startWhenReady: Boolean) {
        try {
            this.startWhenReady = startWhenReady
            isPlaying = false
            isStopped = false
            player?.reset()
            player?.setDataSource(context, uri)
            player?.prepareAsync()
        } catch (e: IOException) {
            onLStop()
            println("播放失败：歌曲文件不存在")
        } catch (e: Exception) {
            onLStop()
        }
    }

    override fun play() {
        if (isPrepared) {
            // 请求音频焦点，失败则取消播放
            if (!requestAudioFocus()) return

            if (player?.isPlaying == false) {
                isPlaying = true
                isStopped = false
                volumeProxy?.fadeStart()
                onLStart()
            }
        }
        onLPlay()
    }

    override fun pause() {
        isPlaying = false
        isStopped = false
        volumeProxy?.fadePause()
        onLPause()
    }

    override fun stop() {
        isPlaying = false
        isPrepared = false
        isStopped = true
        if (player?.isPlaying == true) player?.stop()
        player?.reset()
        player?.release()
        player = null
        onLStop()
    }

    override fun seekTo(durationMs: Number) {
        player?.seekTo(durationMs.toInt())
        onLSeekTo(durationMs)
    }

    override fun requestAudioFocus(): Boolean {
        return true
    }

    override fun getPosition(): Long {
        return player?.currentPosition?.toLong() ?: 0L
    }

    override fun onPrepared(mp: MediaPlayer?) {
        onLPrepared()
        isPrepared = true
        if (startWhenReady) {
            play()
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        onLCompletion()
    }

    override fun onLStart() {
        listener?.onLStart()
    }

    override fun onLStop() {
        listener?.onLStop()
    }

    override fun onLPlay() {
        listener?.onLPlay()
    }

    override fun onLPause() {
        listener?.onLPause()
    }

    override fun onLSeekTo(newDurationMs: Number) {
        listener?.onLSeekTo(newDurationMs)
    }

    override fun onLPrepared() {
        listener?.onLPrepared()
    }

    override fun onLCompletion() {
        listener?.onLCompletion()
    }
}