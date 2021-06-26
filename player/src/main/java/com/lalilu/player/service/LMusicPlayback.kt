package com.lalilu.player.service

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.lalilu.common.LMusicList
import com.lalilu.player.manager.LMusicAudioFocusManager
import com.lalilu.player.manager.LMusicVolumeManager

/**
 *  Playback的一个实现，使用LMusicList作为歌曲队列容器，
 *  歌曲切换逻辑取决于LMusicList
 *  @param mContext 用于MediaPlayer#setDataSource(Context,Uri)中
 *  @param mList 传入一个带有歌曲的LMusicList进行操作
 */
open class LMusicPlayback(
    private val mContext: Context,
    private val mList: LMusicList<String, MediaMetadataCompat>
) : Playback {
    private var mediaPlayer: MediaPlayer? = null
    private var mVolumeManager: LMusicVolumeManager? = null
    private var mAudioFocusManager: LMusicAudioFocusManager? = null
    private var onPlayerCallback: Playback.OnPlayerCallback? = null
    private var playbackState: Int = PlaybackStateCompat.STATE_NONE
    private var isPrepared = false

    fun setAudioFocusManager(mAudioFocusManager: LMusicAudioFocusManager): LMusicPlayback {
        this.mAudioFocusManager = mAudioFocusManager
        return this
    }

    fun setOnPlayerCallback(callback: Playback.OnPlayerCallback): LMusicPlayback {
        this.onPlayerCallback = callback
        return this
    }

    /**
     *  正常播放
     *  isPrepared 用于判断歌曲文件是否已加载完成，否则重新加载
     *  mVolumeManager 用于实现声音渐变的音量控制器
     *
     */
    override fun play() {
        mediaPlayer ?: rebuild()

        if (isPrepared) {
            val result = mAudioFocusManager?.requestAudioFocus()
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return
            mVolumeManager?.fadeStart()
            // mediaPlayer!!.start()
        } else {
            val uri = mList.getNowItem()?.description?.mediaUri ?: return
            playByUri(uri)
            return
        }
        onPlaybackStateChanged(PlaybackStateCompat.STATE_PLAYING)
    }

    /**
     *  正常暂停
     *  mVolumeManager 用于实现声音渐变的音量控制器
     *
     */
    override fun pause() {
        mediaPlayer ?: rebuild()
        mVolumeManager?.fadePause()
        // mediaPlayer!!.pause()
        onPlaybackStateChanged(PlaybackStateCompat.STATE_PAUSED)
    }

    /**
     *  播放暂停切换
     */
    override fun playAndPause() {
        mediaPlayer ?: rebuild()

        if (mediaPlayer!!.isPlaying) pause() else play()
    }

    /**
     *  重建mediaPlayer和mVolumeManager
     */
    override fun rebuild() {
        isPrepared = false
        mediaPlayer = MediaPlayer().also {
            it.setOnPreparedListener {
                isPrepared = true
                play()
            }
            it.setOnCompletionListener { onCompletion() }
        }
        mVolumeManager = LMusicVolumeManager(mediaPlayer!!)
    }

    /**
     *  根据传入的Uri让mediaPlayer进入加载状态
     *  @param uri 歌曲文件的uri
     */
    override fun playByUri(uri: Uri) {
        mediaPlayer ?: rebuild()

        mediaPlayer!!.reset()
        mediaPlayer!!.setDataSource(mContext, uri)
        onMetadataChanged()
        mediaPlayer!!.prepareAsync()
    }

    /**
     *  根据传入的mediaId从mList中获取对应的uri，并传递至playByUri进行加载
     *  @param mediaId 歌曲在MediaStore中分配得到的唯一标识
     */
    override fun playByMediaId(mediaId: String?) {
        val uri = mList.playByKey(mediaId)?.description?.mediaUri ?: return
        playByUri(uri)
    }

    /**
     *  播放下一首
     */
    override fun next() {
        val uri = mList.next()?.description?.mediaUri ?: return
        playByUri(uri)
    }


    /**
     *  播放上一首
     */
    override fun previous() {
        val uri = mList.last()?.description?.mediaUri ?: return
        playByUri(uri)
    }

    /**
     *  停止播放，放弃进度，释放播放器资源
     */
    override fun stop() {
        isPrepared = false
        mediaPlayer!!.stop()
        mediaPlayer!!.release()
        mediaPlayer = null
        onPlaybackStateChanged(PlaybackStateCompat.STATE_STOPPED)
    }

    /**
     *  跳转进度至指定位置，不区分暂停时跳转和播放时跳转
     */
    override fun seekTo(position: Number) {
        mediaPlayer ?: rebuild()

        mediaPlayer!!.seekTo(position.toInt())
        onPlaybackStateChanged(playbackState)
    }

    /**
     *  获取mediaPlayer现在的播放位置
     */
    override fun getPosition(): Long {
        mediaPlayer ?: rebuild()

        return mediaPlayer!!.currentPosition.toLong()
    }

    /**
     *  mediaPlayer播放结束后的动作
     */
    override fun onCompletion() {
        isPrepared = false
        next()
    }

    /**
     *  回调更新PlaybackState
     */
    override fun onPlaybackStateChanged(state: Int) {
        onPlayerCallback?.onPlaybackStateChanged(state)
        playbackState = state
    }

    /**
     *  回调更新MediaMetadata
     */
    override fun onMetadataChanged() {
        onPlayerCallback?.onMetadataChanged(mList.getNowItem())
    }
}