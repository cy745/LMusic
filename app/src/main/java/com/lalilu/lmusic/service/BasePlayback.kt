package com.lalilu.lmusic.service

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.lalilu.lmusic.manager.LMusicAudioFocusManager
import com.lalilu.lmusic.utils.Mathf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

abstract class BasePlayback<T, K, ID>(
    private val mContext: Context
) : Playback<ID>, CoroutineScope {
    private var mediaPlayer: LMusicPlayer? = LMusicPlayer().also {
        it.setOnPreparedListener {
            isPrepared = true
            play()
        }
        it.setOnCompletionListener { onCompletion() }
    }
    abstract var mAudioFocusManager: LMusicAudioFocusManager
    private var playbackState: Int = PlaybackStateCompat.STATE_NONE
    var onPlayerCallback: Playback.OnPlayerCallback? = null

    open var nowPlaying: T? = null
    abstract var nowPlaylist: Flow<K>

    private var isPrepared = false

    abstract fun getUriFromNowItem(nowPlaying: T?): Uri?
    abstract fun getIdFromItem(item: T): ID
    abstract fun getMetaDataFromItem(item: T): MediaMetadataCompat
    abstract fun getItemById(list: K, mediaId: ID): T?

    override fun play() {
        mediaPlayer ?: rebuild()

        if (isPrepared) {
            val result = mAudioFocusManager.requestAudioFocus()
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return
            mediaPlayer?.volumeManager?.fadeStart()
        } else {
            nowPlaying ?: return
            val uri = getUriFromNowItem(nowPlaying) ?: return
            playByUri(uri)
            return
        }
        onPlaybackStateChanged(PlaybackStateCompat.STATE_PLAYING)
    }

    override fun pause() {
        mediaPlayer ?: rebuild()

        mediaPlayer?.volumeManager?.fadePause()
        onPlaybackStateChanged(PlaybackStateCompat.STATE_PAUSED)
    }

    override fun playAndPause() {
        mediaPlayer ?: rebuild()

        if (mediaPlayer!!.isPlaying) pause() else play()
    }

    override fun rebuild() {
        isPrepared = false
        mediaPlayer = LMusicPlayer().also {
            it.setOnPreparedListener {
                isPrepared = true
                play()
            }
            it.setOnCompletionListener { onCompletion() }
        }
    }

    override fun playByUri(uri: Uri) {
        mediaPlayer ?: rebuild()

        mediaPlayer!!.reset()
        mediaPlayer!!.setDataSource(mContext, uri)
        onMetadataChanged()
        mediaPlayer!!.prepareAsync()
    }

    override fun playByMediaId(mediaId: ID?) {
        mediaPlayer ?: rebuild()
        mediaId ?: return

        launch {
            nowPlaylist.collect {
                val list = it ?: return@collect
                val item = getItemById(list, mediaId)
                val uri = getUriFromNowItem(item) ?: return@collect
                nowPlaying = item
                playByUri(uri)
            }
        }
    }

    override fun next() {
        mediaPlayer ?: rebuild()

        launch {
            nowPlaylist.collect {
                val list = it ?: return@collect
                nowPlaying ?: return@collect

                val next = Mathf.clampInLoop(
                    0, getSizeFromList(list) - 1,
                    getIndexOfFromList(list, nowPlaying!!) + 1
                )
                val item = getItemFromListByIndex(list, next) ?: return@collect
                val uri = getUriFromNowItem(item) ?: return@collect
                nowPlaying = item
                playByUri(uri)
            }
        }
    }

    override fun previous() {
        mediaPlayer ?: rebuild()

        launch {
            nowPlaylist.collect {
                val list = it ?: return@collect
                nowPlaying ?: return@collect

                val previous = Mathf.clampInLoop(
                    0, getSizeFromList(list) - 1,
                    getIndexOfFromList(list, nowPlaying!!) - 1
                )

                val item = getItemFromListByIndex(list, previous) ?: return@collect
                val uri = getUriFromNowItem(item) ?: return@collect
                nowPlaying = item
                playByUri(uri)
            }
        }
    }

    abstract fun getSizeFromList(list: K): Int
    abstract fun getIndexOfFromList(list: K, item: T): Int
    abstract fun getItemFromListByIndex(list: K, index: Int): T

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
        nowPlaying ?: return
        onPlayerCallback?.onMetadataChanged(getMetaDataFromItem(nowPlaying!!))
    }

    override fun onPlaybackStateChanged(state: Int) {
        onPlayerCallback?.onPlaybackStateChanged(state)
        playbackState = state
    }
}