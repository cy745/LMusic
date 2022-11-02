package com.lalilu.lmusic.service.playback

import android.content.Context
import android.media.MediaPlayer
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.utils.PlayMode
import java.io.IOException

abstract class LMusicPlayBack<T>(
    private val mContext: Context
) : MediaSessionCompat.Callback(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener {
    private var player: MediaPlayer? = null
    private var volumeProxy: FadeVolumeProxy? = null
    private var isPrepared: Boolean = false
    private var isPlaying: Boolean = false
    private var isStopped: Boolean = true

    companion object {
        var audioSessionId: Int? = null
    }

    abstract fun requestAudioFocus(): Boolean
    abstract fun getCurrent(): T?
    abstract fun getPrevious(random: Boolean): T?
    abstract fun getNext(random: Boolean): T?
    abstract fun getById(id: String): T?
    abstract fun getUriFromItem(item: T): Uri
    abstract fun getMetaDataFromItem(item: T?): MediaMetadataCompat?
    abstract fun getMaxVolume(): Int
    abstract fun getCurrentPlayMode(): PlayMode

    abstract fun onPlayingItemUpdate(item: T?)
    abstract fun onMetadataChanged(metadata: MediaMetadataCompat?)
    abstract fun onPlaybackStateChanged(playbackState: Int)

    abstract fun setRepeatMode(repeatMode: Int)
    abstract fun setShuffleMode(shuffleMode: Int)

    fun getIsStopped(): Boolean = isStopped
    fun getIsPlaying(): Boolean = isPlaying
    fun getPosition(): Long = player?.currentPosition?.toLong() ?: 0L
    fun setMaxVolume(volume: Int) {
        volumeProxy?.setMaxVolume(volume)
    }

    private fun checkPlayer() {
        player = player ?: MediaPlayer().apply {
            setOnPreparedListener(this@LMusicPlayBack)
            setOnCompletionListener(this@LMusicPlayBack)
            volumeProxy = FadeVolumeProxy(this)
            isStopped = false
            LMusicPlayBack.audioSessionId = audioSessionId
        }
        assert(player != null)
        assert(volumeProxy != null)
    }

    override fun onPlay() {
        checkPlayer()
        // 首先判断是否已缓冲完成
        if (isPrepared) {
            // 请求音频焦点，失败则取消播放
            if (!requestAudioFocus()) return

            if (player?.isPlaying == false) {
                isPlaying = true
                volumeProxy!!.fadeStart(getMaxVolume())
                println("onPlay")
                onPlaybackStateChanged(PlaybackState.STATE_PLAYING)
            }
        } else {
            // 若未缓冲完成则获取当前歌曲的信息
            val current = getCurrent()

            // 首先更新当前歌曲信息
            onMetadataChanged(getMetaDataFromItem(current))
            onPlaybackStateChanged(PlaybackState.STATE_BUFFERING)
            onPlayFromUri(current?.let { getUriFromItem(it) }, null)
            onPlayingItemUpdate(current)
        }
    }

    override fun onPause() {
        isPlaying = false
        volumeProxy?.fadePause()
        println("onPause")
        onPlaybackStateChanged(PlaybackState.STATE_PAUSED)
    }

    override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
        try {
            checkPlayer()
            isPlaying = false
            player?.reset()
            player?.setDataSource(mContext, uri!!)
            player?.prepareAsync()
        } catch (e: IOException) {
            onStop()
            ToastUtils.showLong("播放失败：歌曲文件不存在")
        } catch (e: Exception) {
            onStop()
        }
    }

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
        println("onPlayFromMediaId: $mediaId")

        // 首先通过ID获取到对应的歌曲
        val item = getById(mediaId)

        // 更新当前歌曲信息
        onPlayingItemUpdate(item)
        onMetadataChanged(getMetaDataFromItem(item))
        onPlaybackStateChanged(PlaybackState.STATE_BUFFERING)
        onPlayFromUri(item?.let { getUriFromItem(it) }, null)
    }

    override fun onSkipToNext() {
        println("onSkipToNext")

        // 首先获取下一首歌曲
        val random = getCurrentPlayMode() == PlayMode.Shuffle
        val next = getNext(random = random)

        // 更新当前歌曲信息
        onPlayingItemUpdate(next)
        onMetadataChanged(getMetaDataFromItem(next))
        onPlaybackStateChanged(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT)
        onPlayFromUri(next?.let { getUriFromItem(it) }, null)
    }

    override fun onSkipToPrevious() {
        println("onSkipToPrevious")

        // 首先获取上一首歌曲
        val random = getCurrentPlayMode() == PlayMode.Shuffle
        val previous = getPrevious(random = random)

        // 更新当前歌曲信息
        onPlayingItemUpdate(previous)
        onMetadataChanged(getMetaDataFromItem(previous))
        onPlaybackStateChanged(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS)
        onPlayFromUri(previous?.let { getUriFromItem(it) }, null)
    }

    override fun onSeekTo(pos: Long) {
        println("onSeekTo")

        player?.seekTo(pos.toInt())
        onPlaybackStateChanged(PlaybackStateCompat.STATE_PLAYING)
    }

    override fun onStop() {
        println("onStop")

        isPlaying = false
        isPrepared = false
        isStopped = true
        if (player?.isPlaying == true) player?.stop()
        player?.reset()
        player?.release()
        player = null
        audioSessionId = null

        onPlayingItemUpdate(null)
        onMetadataChanged(null)
        onPlaybackStateChanged(PlaybackStateCompat.STATE_STOPPED)
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
        println("onCustomAction: $action")
        when (action) {
            Config.ACTION_PLAY_AND_PAUSE -> {
                if (player?.isPlaying == true) onPause() else onPlay()
            }

            Config.ACTION_RELOAD_AND_PLAY -> {
                isPrepared = false
                onPlay()
            }
        }
    }

    override fun onSetRepeatMode(repeatMode: Int) {
        println("onSetRepeatMode: $repeatMode")
        setRepeatMode(repeatMode)
    }

    override fun onSetShuffleMode(shuffleMode: Int) {
        println("onSetShuffleMode: $shuffleMode")
        setShuffleMode(shuffleMode)
    }

    override fun onPrepared(mp: MediaPlayer?) {
        isPrepared = true
        onPlay()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        isPrepared = false

        when (getCurrentPlayMode()) {
            PlayMode.RepeatOne -> onPlay()
            else -> onSkipToNext()
        }
    }
}