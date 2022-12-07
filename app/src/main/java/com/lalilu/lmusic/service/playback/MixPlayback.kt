package com.lalilu.lmusic.service.playback

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import com.lalilu.lmedia.entity.LSong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MixPlayback @Inject constructor(
    private val localPlayer: LocalPlayer,
    private val remotePlayer: RemotePlayer
) : Playback<LSong>, MediaSessionCompat.Callback(), Player.Listener {

    init {
        localPlayer.listener = this
        remotePlayer.listener = this
    }

    private var currentPlayer = LocalPlayer::class
        set(value) {
            if (field == value) return

            if (value == LocalPlayer::class) {
                remotePlayer.stop()
            } else {
                localPlayer.stop()
            }
            field = value
        }

    override var queue: PlayQueue<LSong>? = null

    private val player: Player
        get() = if (currentPlayer == LocalPlayer::class) localPlayer else remotePlayer

    override fun onPause() {
        player.pause()
    }


    override fun onPlay() {
        if (player.isPrepared) {
            player.play()
        } else {
            val uri = queue?.run {
                getCurrent()?.let { getUriFromItem(it) }
            } ?: return

            onPlayFromUri(uri, null)
        }
    }

    override fun onPlayFromUri(uri: Uri, extras: Bundle?) {
        player.load(uri, true)
    }

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
        val item = queue?.getById(mediaId) ?: return


    }

    override fun onLStart() {
    }

    override fun onLStop() {
    }

    override fun onLPlay() {
    }

    override fun onLPause() {
    }

    override fun onLSeekTo(newDurationMs: Number) {
    }

    override fun onLPrepared() {
    }

    override fun onLCompletion() {
    }
}