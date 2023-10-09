package com.lalilu.lplayer.service

import com.lalilu.lplayer.extensions.PlayerAction
import com.lalilu.lplayer.playback.impl.MixPlayback

object LController {

    fun doAction(action: PlayerAction): Boolean {
        if (!MixPlayback.readyToUse()) return false

        when (action) {
            PlayerAction.Play -> MixPlayback.onPlay()
            PlayerAction.Pause -> MixPlayback.onPause()
            PlayerAction.SkipToNext -> MixPlayback.onSkipToNext()
            PlayerAction.SkipToPrevious -> MixPlayback.onSkipToPrevious()
            is PlayerAction.PlayById -> MixPlayback.onPlayFromMediaId(action.mediaId, null)
            is PlayerAction.SeekTo -> MixPlayback.onSeekTo(action.positionMs)
            is PlayerAction.CustomAction -> MixPlayback.onCustomAction(action.name, null)
            else -> return false
        }
        return true
    }
}