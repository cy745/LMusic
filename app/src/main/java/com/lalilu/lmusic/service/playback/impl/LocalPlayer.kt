package com.lalilu.lmusic.service.playback.impl

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.lmusic.service.playback.Player
import com.lalilu.lmusic.service.playback.helper.FadeVolumeProxy
import com.lalilu.lmusic.utils.EQHelper
import java.io.IOException

class LocalPlayer(
    private val context: Context
) : Player, Player.Listener,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener {
    private var volumeProxy: FadeVolumeProxy? = null
    private var player: MediaPlayer? = null
        get() {
            field = field ?: createPlayer()
            return field
        }

    override var listener: Player.Listener? = null
    override var isPlaying: Boolean = false
    override var isPrepared: Boolean = false
    override var isStopped: Boolean = true
    private var startWhenReady: Boolean = false

    private fun createPlayer(): MediaPlayer {
        return MediaPlayer().apply {
            setOnPreparedListener(this@LocalPlayer)
            setOnCompletionListener(this@LocalPlayer)
            volumeProxy = FadeVolumeProxy(this)
            EQHelper.audioSessionId = this.audioSessionId
        }
    }

    override fun load(uri: Uri, startWhenReady: Boolean) {
        try {
            this.startWhenReady = startWhenReady
            val oldPlayer = player
            val oldVolumeProxy = volumeProxy

            isPlaying = false
            isStopped = false
            player = createPlayer()
            player?.reset()
            player?.setDataSource(context, uri)
            player?.prepareAsync()

            oldVolumeProxy?.fadePause(duration = 800L) {
                oldPlayer?.apply {
                    if (isPlaying) stop()
                    reset()
                    release()
                }
            }
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

            if (!isPlaying) {
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
        player?.apply {
            if (isPlaying) stop()
            reset()
            release()
        }
        player = null
        EQHelper.audioSessionId = null
        onLStop()
    }

    override fun seekTo(durationMs: Number) {
        player?.seekTo(durationMs.toInt())
        onLSeekTo(durationMs)
    }

    override fun destroy() {
        stop()
        volumeProxy = null
        listener = null
    }

    override fun requestAudioFocus(): Boolean {
        return listener?.requestAudioFocus() ?: true
    }

    override fun getPosition(): Long {
        return player?.currentPosition?.toLong() ?: 0L
    }

    override fun setMaxVolume(volume: Int) {
        volumeProxy?.setMaxVolume(volume)
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

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        ToastUtils.showLong("播放异常：$what $extra")
        return false
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