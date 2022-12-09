package com.lalilu.lmusic.service.playback

import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.service.playback.helper.LMusicAudioFocusHelper
import com.lalilu.lmusic.service.playback.helper.LMusicNoisyReceiver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MixPlayback @Inject constructor(
    localPlayer: LocalPlayer,
    remotePlayer: RemotePlayer,
    private val noisyReceiver: LMusicNoisyReceiver,
    private val audioFocusHelper: LMusicAudioFocusHelper
) : MediaSessionCompat.Callback(), Playback<LSong>, Playback.Listener<LSong>, Player.Listener {
    override var listener: Playback.Listener<LSong>? = null

    init {
        localPlayer.listener = this
        remotePlayer.listener = this
        audioFocusHelper.onPlay = ::onPlay
        audioFocusHelper.onPause = ::onPause
        noisyReceiver.onBecomingNoisy = ::onPause
    }

    override var queue: PlayQueue<LSong>? = null
    override val player: Player = localPlayer

    override fun onPause() {
        player.pause()
    }

    override fun onPlay() {
        if (player.isPrepared) {
            player.play()
        } else {
            val item = queue?.getCurrent() ?: return
            val uri = queue?.getUriFromItem(item) ?: return

            onPlayingItemUpdate(item)
            onPlaybackStateChanged(PlaybackStateCompat.STATE_BUFFERING, 0L)
            onPlayFromUri(uri, null)
        }
    }

    override fun onPlayFromUri(uri: Uri, extras: Bundle?) {
        player.load(uri, true)
    }

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
        val item = queue?.getById(mediaId) ?: return
        val uri = queue?.getUriFromItem(item) ?: return

        onPlayingItemUpdate(item)
        onPlaybackStateChanged(PlaybackStateCompat.STATE_BUFFERING, 0L)
        onPlayFromUri(uri, extras)
    }

    override fun onSkipToNext() {
        val next = queue?.getNext(false) ?: return
        val uri = queue?.getUriFromItem(next) ?: return

        onPlayingItemUpdate(next)
        onPlaybackStateChanged(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT, 0L)
        onPlayFromUri(uri, null)
    }

    override fun onSkipToPrevious() {
        val previous = queue?.getPrevious(false) ?: return
        val uri = queue?.getUriFromItem(previous) ?: return

        onPlayingItemUpdate(previous)
        onPlaybackStateChanged(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS, 0L)
        onPlayFromUri(uri, null)
    }

    override fun onSeekTo(pos: Long) {
        player.seekTo(pos)
    }

    override fun onStop() {
        player.stop()
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
        println("onCustomAction: $action")
        when (action) {
            Config.ACTION_PLAY_AND_PAUSE -> {
                if (player.isPlaying) onPause() else onPlay()
            }
//            Config.ACTION_RELOAD_AND_PLAY -> {
//                isPrepared = false
//                onPlay()
//            }
        }
    }

    override fun onSetRepeatMode(repeatMode: Int) {
        listener?.onSetRepeatMode(repeatMode)
    }

    override fun onSetShuffleMode(shuffleMode: Int) {
        listener?.onSetRepeatMode(shuffleMode)
    }

    override fun requestAudioFocus(): Boolean {
        return audioFocusHelper.requestAudioFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    override fun onLStart() {
        onPlaybackStateChanged(PlaybackStateCompat.STATE_PLAYING, player.getPosition())
    }

    override fun onLStop() {
        onPlayingItemUpdate(null)
        noisyReceiver.unregister()
        audioFocusHelper.abandonAudioFocus()
        onPlaybackStateChanged(PlaybackStateCompat.STATE_STOPPED, 0L)
    }

    override fun onLPlay() {
        noisyReceiver.register()
    }

    override fun onLPause() {
        noisyReceiver.unregister()
        onPlaybackStateChanged(PlaybackStateCompat.STATE_PAUSED, player.getPosition())
    }

    override fun onLSeekTo(newDurationMs: Number) {
        onPlaybackStateChanged(PlaybackStateCompat.STATE_PLAYING, newDurationMs.toLong())
    }

    override fun onLPrepared() {
    }

    override fun onLCompletion() {
        onSkipToNext()
    }

    override fun onPlayingItemUpdate(item: LSong?) {
        listener?.onPlayingItemUpdate(item)
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        listener?.onMetadataChanged(metadata)
    }

    override fun onPlaybackStateChanged(playbackState: Int, position: Long) {
        listener?.onPlaybackStateChanged(playbackState, position)
    }
}