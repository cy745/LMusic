package com.lalilu.lplayer.service

import com.lalilu.common.base.Playable
import com.lalilu.lplayer.extensions.PlayerAction
import com.lalilu.lplayer.playback.Playback

class LController(
    private val playback: Playback<Playable>,
) {

    fun doAction(action: PlayerAction): Boolean {
        if (!playback.readyToUse()) return false

        playback.apply {
            when (action) {
                PlayerAction.Play -> onPlay()
                PlayerAction.Pause -> onPause()
                PlayerAction.SkipToNext -> onSkipToNext()
                PlayerAction.SkipToPrevious -> onSkipToPrevious()
                is PlayerAction.PlayById -> onPlayFromMediaId(action.mediaId, null)
                is PlayerAction.SeekTo -> onSeekTo(action.positionMs)
                is PlayerAction.CustomAction -> onCustomAction(action.name, null)
                else -> return false
            }
        }
        return true
    }
}