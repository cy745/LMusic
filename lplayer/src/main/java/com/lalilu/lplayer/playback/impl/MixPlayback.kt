package com.lalilu.lplayer.playback.impl

import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.lalilu.common.base.Playable
import com.lalilu.lplayer.extensions.AudioFocusHelper
import com.lalilu.lplayer.playback.PlayMode
import com.lalilu.lplayer.playback.PlayQueue
import com.lalilu.lplayer.playback.Playback
import com.lalilu.lplayer.playback.Player
import com.lalilu.lplayer.playback.PlayerEvent

class MixPlayback(
    private val audioFocusHelper: AudioFocusHelper,
    override var playbackListener: Playback.Listener<Playable>? = null,
    override var queue: PlayQueue<Playable>? = null,
    override var player: Player? = null,
    override var playMode: PlayMode = PlayMode.ListRecycle,
) : MediaSessionCompat.Callback(), Playback<Playable>, Playback.Listener<Playable>,
    Player.Listener {

    init {
        player?.listener = this
        player?.couldPlayNow =
            { audioFocusHelper.request() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED }
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
        val next = when (playMode) {
            PlayMode.ListRecycle -> queue?.getNext()
            PlayMode.RepeatOne -> queue?.getNext()
            PlayMode.Shuffle -> queue?.getShuffle()
        } ?: return
        val uri = queue?.getUriFromItem(next) ?: return

        if (playMode == PlayMode.Shuffle) {
            queue?.moveToPrevious(item = next)
        }

        onPlayInfoUpdate(next, PlaybackStateCompat.STATE_SKIPPING_TO_NEXT, 0L)
        onPlayFromUri(uri, null)
    }

    override fun onSkipToPrevious() {
        val previous = when (playMode) {
            PlayMode.ListRecycle -> queue?.getPrevious()
            PlayMode.RepeatOne -> queue?.getPrevious()
            PlayMode.Shuffle -> queue?.getNext()
        } ?: return
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

    private var tempNextItem: Playable? = null

    override fun preloadNextItem() {
        tempNextItem = when (playMode) {
            PlayMode.ListRecycle -> queue?.getNext()
            PlayMode.RepeatOne -> queue?.getNext()
            PlayMode.Shuffle -> queue?.getShuffle()
        } ?: return
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

    override fun onPlayerEvent(event: PlayerEvent) {
        when (event) {
            PlayerEvent.OnPlay -> {
                onPlayInfoUpdate(
                    item = queue?.getCurrent(),
                    playbackState = PlaybackStateCompat.STATE_PLAYING,
                    position = player?.getPosition() ?: 0L
                )
            }

            PlayerEvent.OnStart -> {
                val current = queue?.getCurrent()
                current?.let { onItemPlay(it) }
                onPlayInfoUpdate(
                    current,
                    PlaybackStateCompat.STATE_PLAYING,
                    player?.getPosition() ?: 0L
                )
                preloadNextItem()
            }

            PlayerEvent.OnPause -> {
                val current = queue?.getCurrent()
                current?.let { onItemPause(it) }

                onPlayInfoUpdate(
                    item = current,
                    playbackState = PlaybackStateCompat.STATE_PAUSED,
                    position = player?.getPosition() ?: 0L
                )
            }

            PlayerEvent.OnStop -> {
                audioFocusHelper.abandon()

                onPlayInfoUpdate(
                    item = queue?.getCurrent(),
                    playbackState = PlaybackStateCompat.STATE_STOPPED,
                    position = 0L
                )
            }

            is PlayerEvent.OnCompletion -> {
                // 单曲循环模式
                if (playMode == PlayMode.RepeatOne) {
                    val current = queue?.getCurrent() ?: return
                    val uri = queue?.getUriFromItem(current) ?: return

                    onPlayInfoUpdate(current, PlaybackStateCompat.STATE_BUFFERING, 0L)
                    onPlayFromUri(uri, null)
                    return
                }

                // 若Player未完成预加载，即无法直接播放下一首，则进行Playback的切换下一首流程
                if (!event.nextItemReady) {
                    onSkipToNext()
                    return
                }
                if (tempNextItem != null) {
                    if (playMode == PlayMode.Shuffle) {
                        queue?.moveToPrevious(item = tempNextItem!!)
                    }
                    onPlayInfoUpdate(
                        item = tempNextItem,
                        playbackState = PlaybackStateCompat.STATE_PLAYING,
                        position = player?.getPosition() ?: 0L
                    )
                }
            }

            is PlayerEvent.OnSeekTo -> {
                onPlayInfoUpdate(
                    queue?.getCurrent(),
                    PlaybackStateCompat.STATE_PLAYING,
                    event.newDurationMs.toLong()
                )
            }

            is PlayerEvent.OnCreated -> {
                onPlayerCreated(event.playerId)
            }

            PlayerEvent.OnPrepared -> {}
            PlayerEvent.OnNextPrepared -> {}
            is PlayerEvent.OnError -> {}
        }
    }

    override fun onPlayInfoUpdate(item: Playable?, playbackState: Int, position: Long) {
        playbackListener?.onPlayInfoUpdate(item, playbackState, position)
    }

    override fun onSetPlayMode(playMode: PlayMode) {
        this.playMode = playMode
        playbackListener?.onSetPlayMode(playMode)
    }

    override fun onPlayerCreated(id: Any) {
        playbackListener?.onPlayerCreated(id)
    }

    override fun onItemPlay(item: Playable) {
        playbackListener?.onItemPlay(item)
    }

    override fun onItemPause(item: Playable) {
        playbackListener?.onItemPause(item)
    }
}