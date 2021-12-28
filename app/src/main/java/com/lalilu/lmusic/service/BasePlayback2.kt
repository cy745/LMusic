package com.lalilu.lmusic.service

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.lalilu.lmusic.manager.LMusicAudioFocusManager

// TODO: 2021/12/28 尝试重构Playback，不知何时能完成
abstract class BasePlayback2<Item, Y, ID> : Playback<ID>, MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener {
    abstract var mAudioFocusManager: LMusicAudioFocusManager
    private var mPlayer: LMusicPlayer? = reCreatePlayer()
    private var playbackState: Int = PlaybackStateCompat.STATE_NONE
    private var isPrepared: Boolean = false

    var nowPlaying: Item? = null
    var nowPlaylist: List<Item>? = null

    abstract fun getUriFromNowItem(item: Item?): Uri?
    abstract fun getIdFromItem(item: Item): ID
    abstract fun getMetaDataFromItem(item: Item): MediaMetadataCompat
    abstract fun getItemById(list: List<Item>, id: ID): Item?

    private fun reCreatePlayer(): LMusicPlayer = LMusicPlayer().also {
        it.setOnPreparedListener(this)
        it.setOnCompletionListener(this)
    }

    override fun rebuild() {
        isPrepared = false
        mPlayer = reCreatePlayer()
    }

    override fun play() {
        mPlayer ?: rebuild()
        if (isPrepared) {
            val request = mAudioFocusManager.requestAudioFocus()
            if (request != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return
            mPlayer!!.volumeManager.fadeStart()
        } else {

        }
        onPlaybackStateChanged(PlaybackStateCompat.STATE_PLAYING)
    }

    override fun pause() {
        mPlayer ?: rebuild()
        mPlayer!!.volumeManager.fadePause()

        onPlaybackStateChanged(PlaybackStateCompat.STATE_PAUSED)
    }

    override fun playAndPause() {
        mPlayer ?: rebuild()
        if (mPlayer!!.isPlaying) pause() else play()
    }


    override fun playByUri(uri: Uri) {
        mPlayer ?: rebuild()

    }

    override fun playByMediaId(mediaId: ID?) {
        mPlayer ?: rebuild()

    }

    override fun next() {
        mPlayer ?: rebuild()

    }

    override fun previous() {
        mPlayer ?: rebuild()

    }

    override fun stop() {
        isPrepared = false
        mPlayer?.stop()
        mPlayer?.release()
        mPlayer = null
        onPlaybackStateChanged(PlaybackStateCompat.STATE_STOPPED)
    }

    override fun seekTo(position: Number) {
        mPlayer ?: rebuild()
        mPlayer?.seekTo(position.toInt())
        onPlaybackStateChanged(playbackState)
    }

    override fun getPosition(): Long {
        mPlayer ?: rebuild()
        return mPlayer?.currentPosition?.toLong() ?: 0L
    }

    override fun onPrepared(mp: MediaPlayer?) {
        isPrepared = true
        play()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        next()
    }

    override fun onCompletion() {}

    override fun onPlaybackStateChanged(state: Int) {
        TODO("Not yet implemented")
    }

    override fun onMetadataChanged() {
        TODO("Not yet implemented")
    }


}