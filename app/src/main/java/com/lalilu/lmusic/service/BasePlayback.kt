package com.lalilu.lmusic.service

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import com.lalilu.common.Mathf
import com.lalilu.lmusic.manager.LMusicAudioFocusManager
import com.lalilu.lmusic.manager.LMusicVolumeManager

abstract class BasePlayback<T>(
    private val mContext: Context
) : Playback {
    private var mediaPlayer: MediaPlayer? = null
    private var mVolumeManager: LMusicVolumeManager? = null
    private var playbackState: Int = PlaybackStateCompat.STATE_NONE
    protected var mAudioFocusManager: LMusicAudioFocusManager? = null
    protected var onPlayerCallback: Playback.OnPlayerCallback? = null

    open val nowPlaylist: MutableLiveData<List<T>> = MutableLiveData()
    open val nowPlaying: MutableLiveData<T> = MutableLiveData()

    private var isPrepared = false

    abstract fun getUriFromNowItem(nowPlaying: T): Uri
    abstract fun getIdFromItem(item: T): Long
    abstract fun getMetaDataFromItem(item: T): MediaMetadataCompat
    abstract fun getItemById(list: List<T>, mediaId: Long): T?

    override fun play() {
        mediaPlayer ?: rebuild()

        if (isPrepared) {
            val result = mAudioFocusManager?.requestAudioFocus()
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return
            mVolumeManager?.fadeStart()
        } else {
            nowPlaying.value ?: return
            playByUri(getUriFromNowItem(nowPlaying.value!!))
            return
        }
        onPlaybackStateChanged(PlaybackStateCompat.STATE_PLAYING)
    }

    override fun pause() {
        mediaPlayer ?: rebuild()

        mVolumeManager?.fadePause()
        onPlaybackStateChanged(PlaybackStateCompat.STATE_PAUSED)
    }

    override fun playAndPause() {
        mediaPlayer ?: rebuild()

        if (mediaPlayer!!.isPlaying) pause() else play()
    }

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

    override fun playByUri(uri: Uri) {
        mediaPlayer ?: rebuild()

        mediaPlayer!!.reset()
        mediaPlayer!!.setDataSource(mContext, uri)
        onMetadataChanged()
        mediaPlayer!!.prepareAsync()
    }

    override fun playByMediaId(mediaId: Long?) {
        mediaPlayer ?: rebuild()
        mediaId ?: return
        nowPlaylist.value ?: return

        nowPlaying.value = getItemById(nowPlaylist.value!!, mediaId)
        playByUri(getUriFromNowItem(nowPlaying.value!!))
    }

    override fun next() {
        mediaPlayer ?: rebuild()
        nowPlaylist.value ?: return
        val list = nowPlaylist.value!!

        val next = Mathf.clampInLoop(0, list.size - 1, list.indexOf(nowPlaying.value) + 1)
        nowPlaying.value = list[next]

        playByUri(getUriFromNowItem(list[next]))
    }

    override fun previous() {
        mediaPlayer ?: rebuild()
        nowPlaylist.value ?: return
        val list = nowPlaylist.value!!

        val previous = Mathf.clampInLoop(0, list.size - 1, list.indexOf(nowPlaying.value) - 1)
        nowPlaying.value = list[previous]
        playByUri(getUriFromNowItem(list[previous]))
    }

    override fun stop() {
        isPrepared = false
        mediaPlayer!!.stop()
        mediaPlayer!!.release()
        mediaPlayer = null
        onPlaybackStateChanged(PlaybackStateCompat.STATE_STOPPED)
    }

    override fun seekTo(position: Number) {
        mediaPlayer ?: rebuild()

        mediaPlayer!!.seekTo(position.toInt())
        onPlaybackStateChanged(playbackState)
    }

    override fun getPosition(): Long {
        mediaPlayer ?: rebuild()

        return mediaPlayer!!.currentPosition.toLong()
    }

    override fun onCompletion() {
        isPrepared = false
        next()
    }

    override fun onMetadataChanged() {
        nowPlaying.value ?: return
        onPlayerCallback?.onMetadataChanged(getMetaDataFromItem(nowPlaying.value!!))
    }

    override fun onPlaybackStateChanged(state: Int) {
        onPlayerCallback?.onPlaybackStateChanged(state)
        playbackState = state
    }
}