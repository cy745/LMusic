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
            val maxIndex = currentTimeline.windowCount - 1
            val currentIndex = currentMediaItemIndex

            // 获取下一个元素的index
            val nextIndex = (0..maxIndex)
                .filter { (it - currentIndex) / maxIndex.toFloat() > 0.5f } // 获取距离当前元素至少0.5f总长度距离的元素
                .randomOrNull()

            if (nextIndex != null) {
                moveMediaItem(nextIndex, currentIndex)
                seekTo(currentIndex, 0)
            } else {
                val previousIndex = (currentIndex - 1)
                    .takeIf { it >= 0 }
                    ?: maxIndex

                seekTo(previousIndex, 0)
            }
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