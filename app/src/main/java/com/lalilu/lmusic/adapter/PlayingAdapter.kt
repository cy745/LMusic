package com.lalilu.lmusic.adapter

import androidx.media3.common.MediaItem
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.lalilu.R
import com.lalilu.databinding.ItemPlayingBinding
import com.lalilu.lmusic.datasource.extensions.getDuration
import com.lalilu.lmusic.utils.moveHeadToTail
import javax.inject.Inject

class PlayingAdapter @Inject constructor() :
    BaseAdapter<MediaItem, ItemPlayingBinding>(R.layout.item_playing) {

    interface OnItemDragOrSwipedListener {
        fun onDelete(mediaItem: MediaItem)
    }

    var onItemDragOrSwipedListener: OnItemDragOrSwipedListener? = null

    override val itemDragCallback: OnItemTouchCallbackAdapter
        get() = object : OnItemTouchCallbackAdapter() {
            override val swipeFlags: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT

            override fun onDelete(item: MediaItem) {
                onItemDragOrSwipedListener?.onDelete(item)
            }
        }

    override val itemCallback: DiffUtil.ItemCallback<MediaItem>
        get() = object : DiffUtil.ItemCallback<MediaItem>() {
            override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.mediaId == newItem.mediaId
            }

            override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.mediaId == newItem.mediaId &&
                        oldItem.mediaMetadata.title == newItem.mediaMetadata.title &&
                        oldItem.mediaMetadata.getDuration() == newItem.mediaMetadata.getDuration()
            }
        }

    override fun onBind(binding: ItemPlayingBinding, item: MediaItem, position: Int) {
        binding.mediaItem = item
    }

    override fun setDiffNewData(list: MutableList<MediaItem>?) {
        val recyclerView = mRecyclerView?.get() ?: run {
            super.setDiffNewData(list)
            return
        }
        var oldList = data.toMutableList()
        val newList = list?.toMutableList() ?: ArrayList()
        val oldScrollOffset = recyclerView.computeVerticalScrollOffset()
        val oldScrollRange = recyclerView.computeVerticalScrollRange()

        if (newList.isNotEmpty()) {
            // 预先将头部部分差异进行转移
            val size = oldList.indexOfFirst { it.mediaId == newList[0].mediaId }
            if (size > 0 && size >= oldList.size / 2 && oldScrollOffset > oldScrollRange / 2) {
                oldList = oldList.moveHeadToTail(size)

                notifyItemRangeRemoved(0, size)
                notifyItemRangeInserted(oldList.size, size)
            }
        }
        val diffResult = DiffUtil.calculateDiff(
            Callback(oldList, newList, itemCallback),
            false
        )
        data = newList
        diffResult.dispatchUpdatesTo(this)
        if (oldScrollOffset <= 0) {
            recyclerView.scrollToPosition(0)
        }
    }
}