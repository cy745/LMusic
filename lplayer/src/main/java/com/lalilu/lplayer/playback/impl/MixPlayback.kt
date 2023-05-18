package com.lalilu.lplayer.playback.impl

import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lplayer.extensions.AudioFocusHelper
import com.lalilu.lplayer.playback.PlayMode
import com.lalilu.lplayer.playback.PlayQueue
import com.lalilu.lplayer.playback.Playback
import com.lalilu.lplayer.playback.Player

class MixPlayback(
    private val audioFocusHelper: AudioFocusHelper,
    override var playbackListener: Playback.Listener<LSong>? = null,
    override var queue: PlayQueue<LSong>? = null,
    override var player: Player? = null,
    override var playMode: PlayMode = PlayMode.ListRecycle
) : MediaSessionCompat.Callback(), Playback<LSong>, Playback.Listener<LSong>, Player.Listener {

    init {
        player?.listener = this
        audioFocusHelper.onPlay = ::onPlay
        audioFocusHelper.onPause = ::onPause
        audioFocusHelper.isPlaying = { player?.isPlaying ?: false }
    }

    override fun onSetShuffleMode(shuffleMode: Int) {
        onSetPlayMode(PlayMode.of(playMode.repeatMode, shuffleMode))
    }

    override fun onSetRepeatMode(repeatMode: Int) {
        onSetPlayMode(PlayMode.of(repeatMode, playMode.shuffleMode))
    }

    override fun changeToPlayer(changeTo: Player) {
        if (player == changeTo) return

        player?.takeIf { !it.isStopped }?.let {
            it.listener = null
            it.stop()
        }
        player = changeTo
        changeTo.listener = this
    }

    override fun setMaxVolume(volume: Int) {
        player?.setMaxVolume(volume)
    }

    override fun onPause() {
        player?.pause()
    }

    override fun onPlay() {
        if (player?.isPrepared == true) {
            player?.play()
        } else {
            val item = queue?.getCurrent() ?: return
            val uri = queue?.getUriFromItem(item) ?: return

            onPlayInfoUpdate(item, PlaybackStateCompat.STATE_BUFFERING, 0L)
            onPlayFromUri(uri, null)
        }
    }

    override fun onPlayFromUri(uri: Uri, extras: Bundle?) {
        player?.load(uri, true)
    }

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
        val item = queue?.getById(mediaId) ?: return
        val uri = queue?.getUriFromItem(item) ?: return

        onPlayInfoUpdate(item, PlaybackStateCompat.STATE_BUFFERING, 0L)
        onPlayFromUri(uri, extras)
    }

    override fun onSkipToNext() {
        val next = queue?.getNext() ?: return
        val uri = queue?.getUriFromItem(next) ?: return

        onPlayInfoUpdate(next, PlaybackStateCompat.STATE_SKIPPING_TO_NEXT, 0L)
        onPlayFromUri(uri, null)
    }

    override fun onSkipToPrevious() {
        val previous = queue?.getPrevious() ?: return
        val uri = queue?.getUriFromItem(previous) ?: return

        onPlayInfoUpdate(previous, PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS, 0L)
        onPlayFromUri(uri, null)
    }

    override fun onSeekTo(pos: Long) {
        player?.seekTo(pos)
    }

    override fun onStop() {
        player?.stop()
    }

    override fun onCustomActionIn(action: Playback.PlaybackAction?) {
        when (action) {
            Playback.PlaybackAction.PlayPause -> {
                if (player?.isPlaying == true) onPause() else onPlay()
            }

            Playback.PlaybackAction.ReloadAndPlay -> {
                player?.isPrepared = false
                onPlay()
            }

            null -> {

            }
        }
    }

    private var tempNextItem: LSong? = null

    override fun preloadNextItem() {
        tempNextItem = queue?.getNext() ?: return
        val uri = queue?.getUriFromItem(tempNextItem!!) ?: return

        player?.preloadNext(uri)
    }

    override fun destroy() {
        playbackListener = null
        queue = null
        player = null
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
        handleCustomAction(action)
    }

    override fun requestAudioFocus(): Boolean {
        return audioFocusHelper.request() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    override fun onLPlayerCreated(id: Any) {
        onPlayerCreated(id)
    }

    override fun onLStart() {
        val current = queue?.getCurrent()
        current?.let { onItemPlay(it) }
        onPlayInfoUpdate(
            current,
            PlaybackStateCompat.STATE_PLAYING,
            player?.getPosition() ?: 0L
        )
        preloadNextItem()
    }

    override fun onLStop() {
        audioFocusHelper.abandon()

        onPlayInfoUpdate(
            item = queue?.getCurrent(),
            playbackState = PlaybackStateCompat.STATE_STOPPED,
            position = 0L
        )
    }

    override fun onLPlay() {
        onPlayInfoUpdate(
            item = queue?.getCurrent(),
            playbackState = PlaybackStateCompat.STATE_PLAYING,
            position = player?.getPosition() ?: 0L
        )
    }

    override fun onLPause() {
        val current = queue?.getCurrent()
        current?.let { onItemPause(it) }

        onPlayInfoUpdate(
            item = queue?.getCurrent(),
            playbackState = PlaybackStateCompat.STATE_PAUSED,
            position = player?.getPosition() ?: 0L
        )
    }

    override fun onLSeekTo(newDurationMs: Number) {
        onPlayInfoUpdate(
            queue?.getCurrent(),
            PlaybackStateCompat.STATE_PLAYING,
            newDurationMs.toLong()
        )
    }

    override fun onLPrepared() {
    }

    override fun onLCompletion(skipToNext: Boolean) {
        // 单曲循环模式
        if (playMode == PlayMode.RepeatOne) {
            val current = queue?.getCurrent() ?: return
            val uri = queue?.getUriFromItem(current) ?: return

            onPlayInfoUpdate(current, PlaybackStateCompat.STATE_BUFFERING, 0L)
            onPlayFromUri(uri, null)
            return
        }

        if (skipToNext) {
            onSkipToNext()
        } else {
            if (tempNextItem != null) {
                onPlayInfoUpdate(
                    item = tempNextItem,
                    playbackState = PlaybackStateCompat.STATE_PLAYING,
                    position = player?.getPosition() ?: 0L
                )
            }
        }
    }

    override fun onPlayInfoUpdate(item: LSong?, playbackState: Int, position: Long) {
        playbackListener?.onPlayInfoUpdate(item, playbackState, position)
    }

    override fun onSetPlayMode(playMode: PlayMode) {
        when (playMode) {
            PlayMode.Shuffle -> queue?.shuffle()
            PlayMode.ListRecycle -> queue?.recoverOrder()
            else -> {}
        }
        this.playMode = playMode
        playbackListener?.onSetPlayMode(playMode)
    }

    override fun onPlayerCreated(id: Any) {
        playbackListener?.onPlayerCreated(id)
    }

    override fun onItemPlay(item: LSong) {
        playbackListener?.onItemPlay(item)
    }

    override fun onItemPause(item: LSong) {
        playbackListener?.onItemPause(item)
    }
}