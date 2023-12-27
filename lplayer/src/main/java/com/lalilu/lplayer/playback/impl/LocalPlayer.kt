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
    override var listener: Player.Listener? = null
    override val isPlaying: Boolean get() = player?.isPlaying == true
    override val isPrepared: Boolean get() = player?.isPrepared == true
    override val isStopped: Boolean get() = player?.isPlaying != true

    override var couldPlayNow: () -> Boolean = { true }
    override var handleNetUrl: (String) -> String = { it }
    private var startWhenReady: Boolean = false
    private var startPosition: Long = 0L
    private var bufferedPercent: Float = 0f

    private var nextLoadedUri: Uri? = null
    private var player: LMediaPlayer? = null                 // 正在播放时操作用的player
    private var nextPlayer: LMediaPlayer? = null             // 准备好播放下一首的player
    private var preloadingPlayer: LMediaPlayer? = null       // 正在预加载的player
    private val recyclePool = mutableListOf<LMediaPlayer>()  // MediaPlayer复用池
    private val recyclePoolMaxSize = 3                       // MediaPlayer复用池的最大容量，超出的部分将释放

    private fun newPlayer(): LMediaPlayer {
        val player = if (Build.VERSION.SDK_INT >= 34) LMediaPlayer(context) else LMediaPlayer()
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build()
        )
        return player
    }

    /**
     * 获取可用的MediaPlayer
     */
    private fun requireUsablePlayer(): LMediaPlayer {
        while (recyclePool.size > recyclePoolMaxSize) {
            recyclePool.removeLastOrNull()?.let {
                it.reset()
                it.release()
            }
        }
        return recyclePool.removeFirstOrNull() ?: newPlayer()
    }

    /**
     * 回收MediaPlayer，超出最大容量则释放不再需要该MediaPlayer了
     */
    private fun recycleMediaPlayer(player: LMediaPlayer) {
        if (player.isPlaying) stop()
        unbindPlayer(player)
        player.reset()

        if (recyclePool.size > recyclePoolMaxSize) {
            player.release()
        } else {
            recyclePool.add(player)
        }
    }

    private fun bindPlayer(player: MediaPlayer) {
        player.setOnPreparedListener(this@LocalPlayer)
        player.setOnCompletionListener(this@LocalPlayer)
        player.setOnBufferingUpdateListener(this@LocalPlayer)
        player.setOnErrorListener(this@LocalPlayer)
        onPlayerEvent(PlayerEvent.OnCreated(player.audioSessionId))
    }

    private fun unbindPlayer(player: MediaPlayer) {
        player.setOnBufferingUpdateListener(null)
        player.setOnCompletionListener(null)
        player.setOnBufferingUpdateListener(null)
        player.setOnErrorListener(null)
    }

    override fun load(uri: Uri, startWhenReady: Boolean, startPosition: Long) {
        try {
            this.startWhenReady = startWhenReady
            this.startPosition = startPosition

            // 暂停后回收旧的MediaPlayer
            player?.fadePause(duration = 800L) fadePause@{
                recycleMediaPlayer(this@fadePause as LMediaPlayer)
            }

            // 若当前准备播放的uri与预加载的一致，则使用预加载完成的player进行播放
            if (uri == nextLoadedUri && nextPlayer != null) {
                player = nextPlayer?.also { bindPlayer(it) }
                nextPlayer = null
                nextLoadedUri = null
                onPrepared(player)
                return
            } else {
                resetPreloadNext()
            }

            // 正常创建或使用回收的MediaPlayer进行加载后播放逻辑
            player = requireUsablePlayer().also { bindPlayer(it) }
            player?.reset()
            player?.loadSource(context, uri, handleNetUrl)
            player?.prepareAsync()
        } catch (e: IOException) {
            LogUtils.e("播放失败：歌曲文件异常: ${e.message}")
            onPlayerEvent(PlayerEvent.OnError(e))
            onPlayerEvent(PlayerEvent.OnStop)
        } catch (e: Exception) {
            LogUtils.e("播放失败：未知异常: ${e.message}")
            onPlayerEvent(PlayerEvent.OnError(e))
            onPlayerEvent(PlayerEvent.OnStop)
        }
    }

    override fun play() {
        if (isPrepared) {
            // 判断当前是否可以播放
            if (!couldPlayNow()) return

            if (!isPlaying) {
                player?.fadeStart()
                onPlayerEvent(PlayerEvent.OnStart)
            }
        }
        onPlayerEvent(PlayerEvent.OnPlay)
    }

    override fun pause() {
        player?.fadePause()
        onPlayerEvent(PlayerEvent.OnPause)
    }

    override fun stop() {
        player?.apply {
            if (isPlaying) stop()
            reset()
            release()
        }
        player = null
        onPlayerEvent(PlayerEvent.OnStop)
    }

    override fun seekTo(durationMs: Number) {
        if (player?.isPrepared != true) {
            LogUtils.e("Not prepared, can't do seekTo action.")
            return
        }

        player?.seekTo(durationMs.toInt())
        onPlayerEvent(PlayerEvent.OnSeekTo(durationMs))
    }

    override fun destroy() {
        stop()
        listener = null
    }

    override fun preloadNext(uri: Uri) {
        // 若预加载已成功且无参数变化则不重新进行预加载
        if (nextLoadedUri == uri && nextPlayer != null) return

        nextPlayer = null
        nextLoadedUri = null

        // 获取可用的MediaPlayer
        preloadingPlayer = requireUsablePlayer()

        // 异步加载数据
        preloadingPlayer!!.apply {
            setOnPreparedListener {
                // 若回调的MediaPlayer与preloadingPlayer不同，则说明新的调用创建了新的预加载MediaPlayer，需回收该MediaPlayer
                if (preloadingPlayer != it) {
                    recycleMediaPlayer(it as LMediaPlayer)
                    return@setOnPreparedListener
                }

                // 成功后将转移至nextPlayer，标记为待播放
                nextPlayer = it
                nextLoadedUri = uri
                preloadingPlayer = null
                player?.setNextMediaPlayer(this)
                onPlayerEvent(PlayerEvent.OnNextPrepared)
            }
            setOnErrorListener { mp, what, extra ->
                LogUtils.e("预加载异常：$what $extra", uri)
                recycleMediaPlayer(mp as LMediaPlayer)
                false
            }
            reset()
            loadSource(context, uri, handleNetUrl)
            prepareAsync()
        }
    }

    override fun confirmPreloadNext() {
        // 获取当前MediaPlayer缓存的音量后回收该MediaPlayer
        val audioSessionId = player?.audioSessionId
        val volume = audioSessionId?.let { PlayerVolumeHelper.getNowVolume(it) }
        player?.let(::recycleMediaPlayer)

        // 将nextPlayer转移至当前播放的player
        player = nextPlayer?.also { bindPlayer(it) }
        nextPlayer = null
        nextLoadedUri = null

        volume?.let { player?.setVolume(it, it) }
    }

    override fun resetPreloadNext() {
        player?.setNextMediaPlayer(null)

        // 取消播放该预加载元素，此时将已经加载好的Player回收
        nextPlayer?.let(::recycleMediaPlayer)
        nextPlayer = null
        nextLoadedUri = null
    }

    override fun getPosition(): Long {
        if (player?.isPrepared != true) return 0L
        return runCatching { player?.currentPosition?.toLong() }.getOrNull() ?: 0L
    }

    override fun getDuration(): Long {
        if (player?.isPrepared != true) return 0L
        return runCatching { player?.duration?.toLong() }.getOrNull() ?: 0L
    }

    override fun getBufferedPosition(): Long {
        if (player?.isPrepared != true) return 0L
        return (getDuration() * bufferedPercent).toLong()
    }

    override fun getVolume(): Int {
        val audioSessionId = player?.audioSessionId ?: 0
        return PlayerVolumeHelper.getNowVolume(audioSessionId).toInt()
    }

    override fun getMaxVolume(): Int {
        return PlayerVolumeHelper.getMaxVolume().toInt()
    }

    override fun setMaxVolume(volume: Int) {
        player?.setMaxVolume(volume)
    }

    override fun onPrepared(mp: MediaPlayer?) {
        onPlayerEvent(PlayerEvent.OnPrepared)

        // 开始播放时跳转指定position
        if (startPosition > 0L) {
            player?.seekTo(startPosition.toInt())
            startPosition = 0L
        }

        // 是否缓冲完成就开始播放
        if (startWhenReady) {
            play()
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        val readyForNext = nextPlayer != null && nextLoadedUri != null
        onPlayerEvent(PlayerEvent.OnCompletion(readyForNext))
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