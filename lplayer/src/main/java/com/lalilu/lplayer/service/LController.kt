package com.lalilu.lplayer.service

import com.lalilu.common.base.Playable
import com.lalilu.lplayer.extensions.PlayerAction
import com.lalilu.lplayer.extensions.QueueAction
import com.lalilu.lplayer.playback.Playback
import com.lalilu.lplayer.playback.UpdatableQueue

class LController(
    private val playback: Playback<Playable>,
    private val queue: UpdatableQueue<Playable>,
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

    fun doAction(action: QueueAction): Boolean {
        when (action) {
            QueueAction.Clear -> queue.setIds(emptyList())
            QueueAction.Shuffle -> queue.setIds(queue.getIds().shuffled())
            is QueueAction.Remove -> {
                val mediaId = action.id
                if (queue.getCurrentId() == mediaId) {
                    playback.onSkipToNext()
                }
                queue.remove(mediaId)
            }

            is QueueAction.AddToNext -> {
                val mediaId = action.id
                if (mediaId == queue.getCurrentId()) return false

                if (queue.moveToNext(mediaId)) {
                    return true
                } else if (queue.addToNext(mediaId)) {
                    return true
                }
                return false
            }

            is QueueAction.UpdatePlaying -> queue.setCurrentId(action.id)
            is QueueAction.UpdateList -> queue.setIds(action.ids)
        }
        return true
    }
}