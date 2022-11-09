package com.lalilu.lmusic.adapter

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.lalilu.R
import com.lalilu.databinding.ItemPlayingBinding
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.repository.LyricRepository
import com.lalilu.lmusic.utils.extension.launch
import com.lalilu.lmusic.utils.extension.moveHeadToTail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class PlayingAdapter @Inject constructor(
    private val lyricRepository: LyricRepository
) : BaseAdapter<LSong, ItemPlayingBinding>(R.layout.item_playing), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    interface OnItemDragOrSwipedListener {
        fun onDelete(song: LSong): Boolean
        fun onAddToNext(song: LSong): Boolean
    }

    var onItemDragOrSwipedListener: OnItemDragOrSwipedListener? = null

    override val itemDragCallback: OnItemTouchCallbackAdapter
        get() = object : OnItemTouchCallbackAdapter() {
            override val swipeFlags: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT

            override fun onSwiped(item: LSong, direction: Int, position: Int) {
                if (onItemDragOrSwipedListener == null) {
                    recover(position)
                    return
                }
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        val result = onItemDragOrSwipedListener!!.onAddToNext(item)
                        if (result) remove(position) else recover(position)
                    }
                    ItemTouchHelper.RIGHT -> {
                        remove(position)
                        onItemDragOrSwipedListener!!.onDelete(item)
                    }
                }
            }
        }

    override val itemCallback: DiffUtil.ItemCallback<LSong>
        get() = object : DiffUtil.ItemCallback<LSong>() {
            override fun areItemsTheSame(oldItem: LSong, newItem: LSong): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: LSong, newItem: LSong): Boolean {
                return oldItem.id == newItem.id &&
                        oldItem.name == newItem.name &&
                        oldItem.durationMs == newItem.durationMs
            }
        }

    override fun getIdFromItem(item: LSong): String {
        return item.id
    }

    override fun onBind(binding: ItemPlayingBinding, item: LSong, position: Int) {
        binding.song = item
        binding.launch {
            if (isActive) {
                val hasLyric = lyricRepository.hasLyric(item)
                withContext(Dispatchers.Main) {
                    binding.songLrc.visibility =
                        if (hasLyric) View.VISIBLE else View.INVISIBLE
                }
            }
        }
    }

    override fun setDiffNewData(list: MutableList<LSong>?) {
        val recyclerView = mRecyclerView?.get() ?: run {
            super.setDiffNewData(list)
            return
        }

        var oldList = data
        val newList = list ?: ArrayList()
        val oldScrollOffset = recyclerView.computeVerticalScrollOffset()
        val oldScrollRange = recyclerView.computeVerticalScrollRange()

        if (newList.isNotEmpty()) {
            // 预先将头部部分差异进行转移
            val size = oldList.indexOfFirst { it.id == newList[0].id }
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