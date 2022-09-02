package com.lalilu.lmusic.service

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
import com.lalilu.lmusic.utils.FadeVolumeProxy
import java.io.IOException

abstract class LMusicPlayBack<T>(
    private val mContext: Context
) : MediaSessionCompat.Callback(),
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener {
    private var player: MediaPlayer? = null
    private var isPrepared: Boolean = false
    private var volumeProxy: FadeVolumeProxy? = null

    abstract fun requestAudioFocus(): Boolean
    abstract fun getCurrent(): T?
    abstract fun skipToNext(random: Boolean = false)
    abstract fun skipToPrevious(random: Boolean = false)
    abstract fun skipToItemByID(id: String)
    abstract fun getUriFromItem(item: T): Uri
    abstract fun getMetaDataFromItem(item: T?): MediaMetadataCompat?
    abstract fun onMetadataChanged(metadata: MediaMetadataCompat?)
    abstract fun onPlaybackStateChanged(playbackState: Int)

    abstract fun setRepeatMode(repeatMode: Int)
    abstract fun setShuffleMode(shuffleMode: Int)

    fun getPosition(): Long {
        return player?.currentPosition?.toLong() ?: 0L
    }

    private fun checkPlayer() {
        player = player ?: MediaPlayer().apply {
            setOnPreparedListener(this@LMusicPlayBack)
            setOnCompletionListener(this@LMusicPlayBack)
        }
        volumeProxy = FadeVolumeProxy(player!!)
        assert(player != null)
        assert(volumeProxy != null)
    }

    override fun onPlay() {
        checkPlayer()
        // 首先判断是否已缓冲完成
        if (isPrepared) {
            // 请求音频焦点，失败则取消播放
            if (!requestAudioFocus()) return

            onPlaybackStateChanged(PlaybackState.STATE_PLAYING)
            if (player?.isPlaying == false) {
                volumeProxy!!.fadeStart()
            }
        } else {
            val current = getCurrent()

            // 首先更新当前歌曲信息
            onMetadataChanged(getMetaDataFromItem(current))
//            onPlaybackStateChanged(PlaybackState.STATE_BUFFERING)
            onPlayFromUri(current?.let { getUriFromItem(it) }, null)
        }
    }

    override fun onPause() {
        onPlaybackStateChanged(PlaybackState.STATE_PAUSED)
        volumeProxy?.fadePause()
    }

    override fun onPrepare() {
        println("onPrepare")
    }

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
        skipToItemByID(mediaId)
        isPrepared = false
        onPlay()
    }

    override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
        try {
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

    override fun onSkipToNext() {
        // 首先让数据源将游标指向下一个item
        skipToNext()
        val current = getCurrent()

        // 首先更新当前歌曲信息
        onMetadataChanged(getMetaDataFromItem(current))
        onPlaybackStateChanged(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT)
        onPlayFromUri(current?.let { getUriFromItem(it) }, null)
    }

    override fun onSkipToPrevious() {
        // 首先让数据源将游标指向上一个item
        skipToPrevious()
        val current = getCurrent()

        // 首先更新当前歌曲信息
        onMetadataChanged(getMetaDataFromItem(current))
        onPlaybackStateChanged(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS)
        onPlayFromUri(current?.let { getUriFromItem(it) }, null)
    }

    override fun onSeekTo(pos: Long) {
        player?.seekTo(pos.toInt())
        onPlaybackStateChanged(PlaybackStateCompat.STATE_PLAYING)
    }

    override fun onStop() {
        isPrepared = false
        if (player?.isPlaying == true) player?.stop()
        player?.reset()
        player?.release()
        player = null

        onMetadataChanged(null)
        onPlaybackStateChanged(PlaybackStateCompat.STATE_STOPPED)
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
        when (action) {
            Config.ACTION_PLAY_AND_PAUSE -> {
                if (player?.isPlaying == true) onPause() else onPlay()
            }
            Config.ACTION_RELOAD_AND_PLAY -> {
                isPrepared = false
                onPlay()
            }
        }
        println("onCustomAction: $action")
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
        onSkipToNext()
    }
}