package com.lalilu.lplayer.playback.impl

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import com.blankj.utilcode.util.LogUtils
import com.lalilu.lplayer.extensions.PlayerVolumeHelper
import com.lalilu.lplayer.extensions.fadePause
import com.lalilu.lplayer.extensions.fadeStart
import com.lalilu.lplayer.extensions.loadSource
import com.lalilu.lplayer.extensions.setMaxVolume
import com.lalilu.lplayer.playback.Player
import com.lalilu.lplayer.playback.PlayerEvent
import java.io.IOException


class LocalPlayer(
    private val context: Context
) : Player, Player.Listener,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnBufferingUpdateListener {
    private var player: MediaPlayer? = null
        get() {
            field = field ?: newPlayer().also { bindPlayer(it) }
            return field
        }

    override var listener: Player.Listener? = null
    override var isPlaying: Boolean = false
    override var isPrepared: Boolean = false
    override var isStopped: Boolean = true
    override var couldPlayNow: () -> Boolean = { true }
    override var handleNetUrl: (String) -> String = { it }
    private var startWhenReady: Boolean = false
    private var nextPlayer: MediaPlayer? = null
    private var bufferedPercent: Float = 0f

    private fun newPlayer(): MediaPlayer {
        val player = if (Build.VERSION.SDK_INT >= 34) MediaPlayer(context) else MediaPlayer()
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build()
        )
        return player
    }

    private fun bindPlayer(player: MediaPlayer) {
        player.setOnPreparedListener(this@LocalPlayer)
        player.setOnCompletionListener(this@LocalPlayer)
        player.setOnBufferingUpdateListener(this@LocalPlayer)
        onPlayerEvent(PlayerEvent.OnCreated(player.audioSessionId))
    }

    override fun load(uri: Uri, startWhenReady: Boolean) {
        try {
            this.startWhenReady = startWhenReady
            val oldPlayer = player

            isPlaying = false
            isStopped = false
            player = newPlayer().also { bindPlayer(it) }
            player?.reset()
            player?.loadSource(context, uri, handleNetUrl)
            player?.prepareAsync()

            oldPlayer?.fadePause(duration = 800L) {
                oldPlayer.apply {
                    if (isPlaying) stop()
                    reset()
                    release()
                }
            }
        } catch (e: IOException) {
            println("播放失败：歌曲文件异常: ${e.message}")
            onPlayerEvent(PlayerEvent.OnError(e))
            onPlayerEvent(PlayerEvent.OnStop)
        } catch (e: Exception) {
            println("播放失败：未知异常: ${e.message}")
            onPlayerEvent(PlayerEvent.OnError(e))
            onPlayerEvent(PlayerEvent.OnStop)
        }
    }

    override fun play() {
        if (isPrepared) {
            // 判断当前是否可以播放
            if (!couldPlayNow()) return

            if (!isPlaying) {
                isPlaying = true
                isStopped = false
                player?.fadeStart()
                onPlayerEvent(PlayerEvent.OnStart)
            }
        }
        onPlayerEvent(PlayerEvent.OnPlay)
    }

    override fun pause() {
        isPlaying = false
        isStopped = false
        player?.fadePause()
        onPlayerEvent(PlayerEvent.OnPause)
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
        onPlayerEvent(PlayerEvent.OnStop)
    }

    override fun seekTo(durationMs: Number) {
        player?.seekTo(durationMs.toInt())
        onPlayerEvent(PlayerEvent.OnSeekTo(durationMs))
    }

    override fun destroy() {
        stop()
        listener = null
    }

    override fun preloadNext(uri: Uri) {
        // 创建MediaPlayer，异步加载数据，并且在加载完成后调用setNextMediaPlayer方法
        newPlayer().apply {
            loadSource(context, uri, handleNetUrl)
            setOnPreparedListener {
                nextPlayer = this
                player?.setNextMediaPlayer(this)
                onPlayerEvent(PlayerEvent.OnNextPrepared)
            }
            prepareAsync()
        }
    }

    override fun getPosition(): Long {
        return runCatching { player?.currentPosition?.toLong() }.getOrNull() ?: 0L
    }

    override fun getDuration(): Long {
        return runCatching { player?.duration?.toLong() }.getOrNull() ?: 0L
    }

    override fun getBufferedPosition(): Long {
        return (getDuration() * bufferedPercent).toLong()
    }

    override fun getVolume(): Int {
        return PlayerVolumeHelper.getNowVolume(player!!.audioSessionId).toInt()
    }

    override fun getMaxVolume(): Int {
        return PlayerVolumeHelper.getMaxVolume().toInt()
    }

    override fun setMaxVolume(volume: Int) {
        player?.setMaxVolume(volume)
    }

    override fun onPrepared(mp: MediaPlayer?) {
        onPlayerEvent(PlayerEvent.OnPrepared)
        isPrepared = true
        if (startWhenReady) {
            play()
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        val readyForNext = nextPlayer != null
        if (readyForNext) {
            val volume = PlayerVolumeHelper.getNowVolume(player!!.audioSessionId)
            player?.reset()
            player?.release()

            player = nextPlayer
            player?.also { bindPlayer(it) }
            player?.setVolume(volume, volume)
            player?.start()
        }
        onPlayerEvent(PlayerEvent.OnCompletion(readyForNext))
        nextPlayer = null
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        LogUtils.e("播放异常：$what $extra")
        return false
    }

    override fun onPlayerEvent(event: PlayerEvent) {
        listener?.onPlayerEvent(event)
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        bufferedPercent = percent / 100f
    }
}