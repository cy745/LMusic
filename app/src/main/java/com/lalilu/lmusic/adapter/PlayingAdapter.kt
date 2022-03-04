package com.lalilu.lmusic.adapter

import android.annotation.SuppressLint
import androidx.media3.common.MediaItem
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.R
import com.lalilu.databinding.ItemPlayingBinding
import com.lalilu.lmusic.datasource.extensions.getDuration
import com.lalilu.lmusic.utils.moveHeadToTail
import javax.inject.Inject

class PlayingAdapter @Inject constructor() :
    BaseAdapter<MediaItem, ItemPlayingBinding>(R.layout.item_playing) {

    override val itemCallback: DiffUtil.ItemCallback<MediaItem>
        get() = object : DiffUtil.ItemCallback<MediaItem>() {
            @SuppressLint("UnsafeOptInUsageError")
            override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.mediaId == newItem.mediaId
            }

            @SuppressLint("UnsafeOptInUsageError")
            override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.mediaId == newItem.mediaId &&
                        oldItem.mediaMetadata.title == newItem.mediaMetadata.title &&
                        oldItem.mediaMetadata.getDuration() == newItem.mediaMetadata.getDuration()
            }
        }

    override fun onBind(binding: ItemPlayingBinding, item: MediaItem) {
        binding.mediaItem = item
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mRecyclerView = recyclerView
    }

    private var mRecyclerView: RecyclerView? = null

    override fun setDiffNewData(list: MutableList<MediaItem>?) {
        if (mRecyclerView == null) {
            super.setDiffNewData(list)
            return
        }
        var oldList = data.toMutableList()
        val newList = list?.toMutableList() ?: ArrayList()

        if (newList.isNotEmpty()) {
            // 预先将头部部分差异进行转移
            val size = oldList.indexOfFirst { it.mediaId == newList[0].mediaId }
            if (size > 0 && size >= oldList.size / 2 &&
                mRecyclerView!!.computeVerticalScrollOffset() >
                mRecyclerView!!.computeVerticalScrollRange() / 2
            ) {
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
        mRecyclerView!!.scrollToPosition(0)
    }
}