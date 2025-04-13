package com.lalilu.lplayer.extensions

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ShuffleOrder
import kotlin.math.abs
import kotlin.math.min

@OptIn(UnstableApi::class)
internal class QueueControlPlayer(player: ExoPlayer) : ForwardingPlayer(player), Player.Listener {

    init {
        player.addListener(this)
        player.setShuffleOrder(CustomShuffleOrder(0))
    }

    private fun tryMoveNext() {
        if (playMode == PlayMode.Shuffle) {
            val target = getRandomNextIndex()
            if (target < 0) return

            val targetMediaItem = currentTimeline
                .getWindow(target, Timeline.Window())
                .mediaItem
            val nextMediaItem = currentTimeline
                .getWindow(nextMediaItemIndex, Timeline.Window())
                .mediaItem
            replaceMediaItem(target, nextMediaItem)
            replaceMediaItem(nextMediaItemIndex, targetMediaItem)
        }
    }

    private fun getRandomNextIndex(): Int {
        if (currentTimeline.windowCount <= 0) return -1

        val maxIndex = currentTimeline.windowCount - 1
        val currentIndex = currentMediaItemIndex

        // 获取下一个元素的index
        val nextIndex = (0..maxIndex)
            .filter {
                min(
                    abs(it - currentIndex),
                    abs(maxIndex - currentIndex + it)
                ) / maxIndex.toFloat() > 0.25f
            }
            .randomOrNull()
            ?: nextMediaItemIndex

        return nextIndex
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO || reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
            tryMoveNext()
        }
    }

    override fun setShuffleModeEnabled(shuffleModeEnabled: Boolean) {
        super.setShuffleModeEnabled(shuffleModeEnabled)
        tryMoveNext()
    }

    override fun seekToNext() {
        tryMoveNext()
        super.seekToNext()
    }

    override fun seekToPrevious() {
        super.seekToPrevious()
        tryMoveNext()
    }

    @UnstableApi
    private class CustomShuffleOrder(private val size: Int) : ShuffleOrder {
        override fun getLength(): Int {
            return size
        }

        override fun getNextIndex(index: Int): Int {
            val target = index - 1
            return if (target < 0) size - 1 else target
        }

        override fun getPreviousIndex(index: Int): Int {
            val target = index + 1
            return if (target >= size) 0 else target
        }

        override fun getLastIndex(): Int {
            return if (size > 0) size - 1 else C.INDEX_UNSET
        }

        override fun getFirstIndex(): Int {
            return if (size > 0) 0 else C.INDEX_UNSET
        }

        override fun cloneAndInsert(insertionIndex: Int, insertionCount: Int): ShuffleOrder {
            return CustomShuffleOrder(length + insertionCount)
        }

        override fun cloneAndRemove(indexFrom: Int, indexToExclusive: Int): ShuffleOrder {
            return CustomShuffleOrder(length - indexToExclusive + indexFrom)
        }

        override fun cloneAndClear(): ShuffleOrder {
            return CustomShuffleOrder(0)
        }
    }
}


@OptIn(UnstableApi::class)
internal fun ExoPlayer.setUpQueueControl(): Player {
    return QueueControlPlayer(this)
}