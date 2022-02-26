package com.lalilu.lmusic.service.playback

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.lalilu.R
import com.lalilu.lmusic.manager.LMusicAudioFocusManager
import com.lalilu.lmusic.utils.ToastUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.IOException
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
abstract class FlowPlayback<ITEM, LIST, ID>(
    private val mContext: Context
) : Playback<ID>, CoroutineScope,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener,
    AudioManager.OnAudioFocusChangeListener {
    private val logger = Logger.getLogger(this.javaClass.name)
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private var player: LMusicPlayer? = createPlayer()
        get() = field ?: createPlayer()

    @Volatile
    private var isPrepared = false
    private var playbackState = STATE_NONE

    abstract fun getPreviousItemFromListByNowID(list: LIST, id: ID?): ITEM?
    abstract fun getNextItemFromListByNowID(list: LIST, id: ID?): ITEM?
    abstract fun getItemFromListByID(list: LIST, id: ID?): ITEM?
    abstract fun getUriFromItem(item: ITEM): Uri
    abstract fun getMetaDataFromItem(item: ITEM): MediaMetadataCompat

    abstract val onPlayerCallback: Playback.OnPlayerCallback?
    abstract val mAudioFocusManager: LMusicAudioFocusManager
    abstract val mToastUtil: ToastUtil

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
            player?.fadeStart()

            launch {
                onPlaybackStateChanged(STATE_PLAYING)
            }
        } else {
            launch {
                isPrepared = false
                val now = playing.firstOrNull() ?: return@launch

                onMetadataChanged(getMetaDataFromItem(now))
                playByUri(getUriFromItem(now))
            }
        }
    }

    override fun pause() {
        player?.fadePause()

        launch {
            onPlaybackStateChanged(STATE_PAUSED)
        }
    }

    override fun playAndPause() {
        player ?: return
        if (player!!.isPlaying) pause() else play()
    }

    override fun playByUri(uri: Uri) {
        try {
            player?.reset()
            player?.setDataSource(mContext, uri)
            player?.prepareAsync()
        } catch (e: IOException) {
            logger.warning(e.message)
            launch {
                onPlaybackStateChanged(STATE_STOPPED)
                mToastUtil.show(R.string.song_non_exist_tips)
            }
        } catch (e: Exception) {
            logger.warning(e.message)
        }
    }

    override fun playByMediaId(mediaId: ID?) {
        mediaId ?: return

        player?.reset()
        launch {
            val list = listFlow.firstOrNull() ?: return@launch
            val now = getItemFromListByID(list, mediaId) ?: return@launch

            onMetadataChanged(getMetaDataFromItem(now))
            playByUri(getUriFromItem(now))
        }
    }

    override fun next() {
        player?.reset()
        launch {
            val next = next.firstOrNull() ?: return@launch

            onMetadataChanged(getMetaDataFromItem(next))
            playByUri(getUriFromItem(next))
        }
    }

    override fun previous() {
        player?.reset()
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

        launch {
            onPlaybackStateChanged(STATE_STOPPED)
        }
    }

    override fun seekTo(position: Number) {
        if (!isPrepared) return

        player?.seekTo(position.toInt())
        launch {
            onPlaybackStateChanged(playbackState)
        }
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

    override suspend fun onPlaybackStateChanged(state: Int) =
        withContext(Dispatchers.IO) {
            onPlayerCallback?.onPlaybackStateChanged(state)
            playbackState = state
        }

    override suspend fun onMetadataChanged(mediaMetadataCompat: MediaMetadataCompat): Unit =
        withContext(Dispatchers.IO) {
            onPlayerCallback?.onMetadataChanged(mediaMetadataCompat)
        }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
        }
    }
}