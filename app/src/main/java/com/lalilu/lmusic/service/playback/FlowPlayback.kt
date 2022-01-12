package com.lalilu.lmusic.service.playback

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.lalilu.lmusic.manager.LMusicAudioFocusManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
abstract class FlowPlayback<ITEM, LIST, ID>(
    private val mContext: Context
) : Playback<ID>, CoroutineScope,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener,
    AudioManager.OnAudioFocusChangeListener {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private var player: LMusicPlayer? = createPlayer()
        get() = field ?: createPlayer()

    private var isPrepared = false
    private var playbackState = STATE_NONE

    abstract fun getPreviousItemFromListByNowID(list: LIST, id: ID?): ITEM?
    abstract fun getNextItemFromListByNowID(list: LIST, id: ID?): ITEM?
    abstract fun getItemFromListByID(list: LIST, id: ID?): ITEM?
    abstract fun getUriFromItem(item: ITEM): Uri
    abstract fun getMetaDataFromItem(item: ITEM): MediaMetadataCompat

    abstract val onPlayerCallback: Playback.OnPlayerCallback?
    abstract val mAudioFocusManager: LMusicAudioFocusManager
    abstract val mediaIdFlow: Flow<ID?>
    abstract val listFlow: Flow<LIST>
    abstract val repeatModeFlow: Flow<Int>

    abstract val playing: Flow<ITEM?>
    abstract val previous: Flow<ITEM?>
    abstract val next: Flow<ITEM?>

    private fun createPlayer(): LMusicPlayer {
        return LMusicPlayer().also {
            it.setOnPreparedListener(this)
            it.setOnCompletionListener(this)
        }
    }

    override fun play() {
        if (isPrepared) {
            val result = mAudioFocusManager.requestAudioFocus()
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return
            player?.volumeManager?.fadeStart()

            onPlaybackStateChanged(STATE_PLAYING)
        } else {
            launch {
                val now = playing.firstOrNull() ?: return@launch
                playByUri(getUriFromItem(now))
            }
        }
    }

    override fun pause() {
        player?.volumeManager?.fadePause()
        onPlaybackStateChanged(STATE_PAUSED)
    }

    override fun playAndPause() {
        player ?: return
        if (player!!.isPlaying) pause() else play()
    }

    override fun playByUri(uri: Uri) {
        player?.reset()
        player?.setDataSource(mContext, uri)
        player?.prepareAsync()
    }

    override fun playByMediaId(mediaId: ID?) {
        mediaId ?: return

        launch {
            val list = listFlow.firstOrNull() ?: return@launch
            val now = getItemFromListByID(list, mediaId) ?: return@launch

            onMetadataChanged(getMetaDataFromItem(now))
            playByUri(getUriFromItem(now))
        }
    }

    override fun next() {
        launch {
            val next = next.firstOrNull() ?: return@launch

            onMetadataChanged(getMetaDataFromItem(next))
            playByUri(getUriFromItem(next))
        }
    }

    override fun previous() {
        launch {
            val previous = previous.firstOrNull() ?: return@launch

            onMetadataChanged(getMetaDataFromItem(previous))
            playByUri(getUriFromItem(previous))
        }
    }

    override fun stop() {
        isPrepared = false
        player?.stop()
        player?.release()
        player = null
        onPlaybackStateChanged(STATE_STOPPED)
    }

    override fun seekTo(position: Number) {
        if (!isPrepared) return

        player?.seekTo(position.toInt())
        onPlaybackStateChanged(playbackState)
    }

    override fun getPosition(): Long {
        return player?.currentPosition?.toLong() ?: 0L
    }

    override fun onPrepared(p0: MediaPlayer?) {
        isPrepared = true
        play()
    }

    override fun onCompletion(p0: MediaPlayer?) {
        isPrepared = false

        launch {
            when (repeatModeFlow.firstOrNull()) {
                REPEAT_MODE_ALL -> next()
                REPEAT_MODE_ONE -> play()
                else -> next()
            }
        }
    }

    override fun onPlaybackStateChanged(state: Int) {
        onPlayerCallback?.onPlaybackStateChanged(state)
        playbackState = state
    }

    override fun onMetadataChanged(mediaMetadataCompat: MediaMetadataCompat) {
        onPlayerCallback?.onMetadataChanged(mediaMetadataCompat)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
        }
    }
}