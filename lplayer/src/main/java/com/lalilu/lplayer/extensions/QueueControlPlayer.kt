package com.lalilu.lplayer.extensions

import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ShuffleOrder

@OptIn(UnstableApi::class)
internal class QueueControlPlayer(player: Player) : ForwardingPlayer(player) {
    override fun seekToNext() {
        if (playMode == PlayMode.Shuffle) {
            // TODO 待完善随机播放模式的列表变换效果
            super.seekToPrevious()
            return
        }

        super.seekToNext()
    }

    override fun seekToPrevious() {
        if (playMode == PlayMode.Shuffle) {
            super.seekToNext()
            return
        }

        super.seekToPrevious()
    }
}

@OptIn(UnstableApi::class)
internal fun ExoPlayer.setUpQueueControl(): Player {
    return QueueControlPlayer(
        this.apply { setShuffleOrder(ShuffleOrder.UnshuffledShuffleOrder(0)) }
    )
}