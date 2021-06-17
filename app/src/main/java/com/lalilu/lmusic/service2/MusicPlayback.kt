package com.lalilu.lmusic.service2

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.support.v4.media.session.PlaybackStateCompat
import com.lalilu.lmusic.utils.LMusicVolumeManager


class MusicPlayback(private val playbackListener: MusicPlaybackListener) :
    MediaPlayer.OnPreparedListener {

    interface MusicPlaybackListener :
        MediaPlayer.OnCompletionListener {
        fun notifyNewPlayBackState(state: PlaybackStateCompat)
    }

    private lateinit var mVolumeManager: LMusicVolumeManager
    private var mPlayer: MediaPlayer? = null
    private var mState: Int = 0
    private var mPositionAtPause: Long = -1
    private var lastUri: Uri? = null

    init {
        initMediaPlayer()
    }

    private fun initMediaPlayer() {
        mPlayer = MediaPlayer().also {
            it.setOnPreparedListener(this)
            it.setOnCompletionListener(playbackListener)
        }
        mVolumeManager = LMusicVolumeManager(mPlayer!!)
    }

    fun toggle() {
        when (mState) {
            PlaybackStateCompat.STATE_PLAYING -> pause()
            PlaybackStateCompat.STATE_PAUSED -> play()
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        play()
    }

    private fun play() {
        if (mPlayer == null) initMediaPlayer()
        if (mPositionAtPause >= 0) {
            mPlayer?.seekTo(mPositionAtPause.toInt())
            mPositionAtPause = -1
        }
        mVolumeManager.fadeStart()
        setNewPlayBackState(PlaybackStateCompat.STATE_PLAYING)
    }

    fun playFromUri(mediaUri: Uri, context: Context) {
        if (mPlayer == null) initMediaPlayer()
        if (lastUri == mediaUri) {
            play()
            return
        }
        lastUri = mediaUri

        mPlayer?.reset()
        mPlayer?.setDataSource(context, mediaUri)
        mPlayer?.prepare()
    }

    fun pause() {
        if (mPlayer != null && mPlayer!!.isPlaying) {
            mVolumeManager.fadePause()
            setNewPlayBackState(PlaybackStateCompat.STATE_PAUSED)
        }
    }

    fun seekTo(num: Number) {
        if (mState == PlaybackStateCompat.STATE_PLAYING) {
            mPlayer?.seekTo(num.toInt())
        } else {
            mPositionAtPause = num.toLong()
        }
        setNewPlayBackState(mState)
    }

    fun stop() {
        if (mPlayer != null) {
            mPlayer!!.reset()
            mPlayer!!.release()
            mPlayer = null
        }
        setNewPlayBackState(PlaybackStateCompat.STATE_STOPPED)
    }

    private fun getPosition(): Long {
        return if (mPositionAtPause >= 0) {
            mPositionAtPause
        } else {
            (mPlayer?.currentPosition ?: 0).toLong()
        }
    }

    private fun setNewPlayBackState(@PlaybackStateCompat.State newState: Int) {
        mState = newState

        val state = PlaybackStateCompat.Builder()
            .setState(mState, getPosition(), 1.0f)
        playbackListener.notifyNewPlayBackState(state.build())
    }


}