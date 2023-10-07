package com.lalilu.lplayer.playback.impl

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import com.blankj.utilcode.util.LogUtils
import com.lalilu.lplayer.extensions.fadePause
import com.lalilu.lplayer.extensions.fadeStart
import com.lalilu.lplayer.extensions.getNowVolume
import com.lalilu.lplayer.extensions.setMaxVolume
import com.lalilu.lplayer.playback.Player
import java.io.IOException
import java.net.URLDecoder


class LocalPlayer(
    private val context: Context
) : Player, Player.Listener,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener {
    private var player: MediaPlayer? = null
        get() {
            field = field ?: MediaPlayer().also { bindPlayer(it) }
            return field
        }

    override var listener: Player.Listener? = null
    override var isPlaying: Boolean = false
    override var isPrepared: Boolean = false
    override var isStopped: Boolean = true
    private var startWhenReady: Boolean = false

    fun bindPlayer(player: MediaPlayer) {
        player.setOnPreparedListener(this@LocalPlayer)
        player.setOnCompletionListener(this@LocalPlayer)
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build()
        )
        onLPlayerCreated(player.audioSessionId)
    }

    override fun load(uri: Uri, startWhenReady: Boolean) {
        try {
            this.startWhenReady = startWhenReady
            val oldPlayer = player

            // TODO 待解决Player切换时的各种问题
            isPlaying = false
            isStopped = false
            player = MediaPlayer().also { bindPlayer(it) }
            player?.reset()
            if (uri.scheme == "content" || uri.scheme == "file") {
                player?.setDataSource(context, uri)
            } else {
                // url 的长度可能会超长导致异常
                val url = URLDecoder.decode(uri.toString(), "UTF-8")
                player?.setDataSource(url)
            }

            player?.prepareAsync()

            oldPlayer?.fadePause(duration = 800L) {
                oldPlayer.apply {
                    if (isPlaying) stop()
                    reset()
                    release()
                }
            }
        } catch (e: IOException) {
            onLStop()
            println("播放失败：歌曲文件异常: ${e.message}")
        } catch (e: Exception) {
            println("播放失败：未知异常: ${e.message}")
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
                player?.fadeStart()
                onLStart()
            }
        }
        onLPlay()
    }

    override fun pause() {
        isPlaying = false
        isStopped = false
        player?.fadePause()
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
        onLStop()
    }

    override fun seekTo(durationMs: Number) {
        player?.seekTo(durationMs.toInt())
        onLSeekTo(durationMs)
    }

    override fun destroy() {
        stop()
        listener = null
    }

    private var nextPlayer: MediaPlayer? = null

    override fun preloadNext(uri: Uri) {
        // 创建MediaPlayer，异步加载数据，并且在加载完成后调用setNextMediaPlayer方法
        MediaPlayer().apply {
            setDataSource(context, uri)
            setOnPreparedListener {
                nextPlayer = this
                player?.setNextMediaPlayer(this)
            }
            prepareAsync()
        }
    }

    override fun requestAudioFocus(): Boolean {
        return listener?.requestAudioFocus() ?: true
    }

    override fun onLPlayerCreated(id: Any) {
        listener?.onLPlayerCreated(id)
    }

    override fun getPosition(): Long {
        return player?.currentPosition?.toLong() ?: 0L
    }

    override fun setMaxVolume(volume: Int) {
        player?.setMaxVolume(volume)
    }

    override fun onPrepared(mp: MediaPlayer?) {
        onLPrepared()
        isPrepared = true
        if (startWhenReady) {
            play()
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        val readyForNext = nextPlayer != null
        if (readyForNext) {
            val volume = getNowVolume(player!!.audioSessionId)
            player = nextPlayer
            player?.setVolume(volume, volume)
            player?.start()
            player?.apply {
                setOnPreparedListener(this@LocalPlayer)
                setOnCompletionListener(this@LocalPlayer)
            }
        }
        onLCompletion(skipToNext = !readyForNext)
        nextPlayer = null
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        LogUtils.e("播放异常：$what $extra")
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

    override fun onLCompletion(skipToNext: Boolean) {
        listener?.onLCompletion(skipToNext)
    }
}