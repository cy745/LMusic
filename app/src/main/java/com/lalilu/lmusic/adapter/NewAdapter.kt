package com.lalilu.lmusic.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.lalilu.R
import com.lalilu.common.base.Playable
import com.lalilu.databinding.ItemPlayingBinding
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.utils.extension.durationToTime
import com.lalilu.lmusic.utils.extension.getMimeTypeIconRes
import com.lalilu.lmusic.utils.extension.moveHeadToTail
import com.lalilu.lmusic.utils.extension.removeAt

abstract class NewAdapter<I : Any, B : ViewBinding> constructor(
    private val layoutId: Int,
) : RecyclerView.Adapter<NewAdapter<I, B>.NewViewHolder>(), View.OnClickListener,
    View.OnLongClickListener {
    protected var data: List<I> = emptyList()

    abstract fun onBind(binding: B, item: I, position: Int)
    abstract fun getIdFromItem(item: I): String

    fun updateByItem(item: I) {
        updateByItemId(getIdFromItem(item))
    }

    fun updateByItemId(id: String) {
        val position = data.indexOfFirst { id == getIdFromItem(it) }
        if (position in data.indices) {
            notifyItemChanged(position)
        }
    }

    inner class NewViewHolder constructor(internal val binding: B) :
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: NewAdapter<I, B>.NewViewHolder, position: Int) {
        val binding = holder.binding
        val item = data[position]
        binding.root.tag = item
        binding.root.setOnClickListener(this)
        binding.root.setOnLongClickListener(this)
        onBind(binding, item, position)
    }
}

enum class ViewEvent {
    OnClick, OnLongClick, OnSwipeLeft, OnSwipeRight, OnBind
}

fun interface OnViewEvent<T> {
    fun onViewEvent(event: ViewEvent, item: T)
}

fun interface OnItemBoundCallback<T> {
    fun onItemBound(binding: ItemPlayingBinding, item: T)
}

fun interface OnDataUpdatedCallback {
    fun onDataUpdated(needScrollToTop: Boolean)
}

class NewPlayingAdapter private constructor(
    private val onViewEvent: OnViewEvent<Playable>?,
    private val onItemBoundCallback: OnItemBoundCallback<Playable>?,
    private val onDataUpdatedCallback: OnDataUpdatedCallback?,
    private val itemCallback: ItemCallback<Playable>?,
) : NewAdapter<Playable, ItemPlayingBinding>(R.layout.item_playing) {

    private val diffUtilCallbackHelper =
        itemCallback?.let { DiffUtilCallbackHelper(itemCallback = it) }
    private val touchHelper = TouchHelper { position, direction ->
        val item = data.getOrNull(position) ?: return@TouchHelper

        val remove = when (direction) {
            ItemTouchHelper.LEFT -> position !in 0..1
            else -> true
        }

        if (remove) {
            notifyItemRemoved(position)
            data = data.removeAt(position)
        } else {
            notifyItemChanged(position)
        }

        when (direction) {
            ItemTouchHelper.LEFT -> onViewEvent?.onViewEvent(ViewEvent.OnSwipeLeft, item)
            ItemTouchHelper.RIGHT -> onViewEvent?.onViewEvent(ViewEvent.OnSwipeRight, item)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        ItemTouchHelper(touchHelper).attachToRecyclerView(recyclerView)
    }


    override fun onBind(binding: ItemPlayingBinding, item: Playable, position: Int) {
        binding.songTitle.text = item.title
        binding.songSinger.text = item.subTitle
        binding.songDuration.text = item.durationMs.durationToTime()
        binding.songPic.setRoundOutline(2)
        binding.songPic.loadCoverForPlaying(item)

        if (item is LSong) {
            binding.songType.setImageResource(getMimeTypeIconRes(item.mimeType))
        }

        onItemBoundCallback?.onItemBound(binding, item)
    }

    override fun getIdFromItem(item: Playable): String {
        return item.mediaId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewViewHolder {
        return NewViewHolder(
            ItemPlayingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onClick(v: View?) {
        (v?.tag as? Playable)?.let { onViewEvent?.onViewEvent(ViewEvent.OnClick, it) }
    }

    override fun onLongClick(v: View?): Boolean {
        v?.parent?.requestDisallowInterceptTouchEvent(true)
        (v?.tag as? Playable)?.let { onViewEvent?.onViewEvent(ViewEvent.OnLongClick, it) }
        return true
    }

    fun setDiffData(list: List<Playable>) {
        var needScrollToTop = false
        if (itemCallback == null || diffUtilCallbackHelper == null) {
            data = list
            notifyDataSetChanged()
            onDataUpdatedCallback?.onDataUpdated(false)
        }

        var oldList = data
        if (list.isNotEmpty() && oldList.isNotEmpty()) {
            // 排除播放上一首的情况
            if (oldList.lastOrNull()?.mediaId == list[0].mediaId) {
                needScrollToTop = true
            } else {
                // 预先将头部部分差异进行转移
                // 通过比对第一个元素的id来判断是否需要转移
                val size = oldList.indexOfFirst { it.mediaId == list[0].mediaId }
                if (size > 0) {
                    oldList = oldList.moveHeadToTail(size)

                    notifyItemRangeRemoved(0, size)
                    notifyItemRangeInserted(oldList.size, size)
                    needScrollToTop = true
                }
            }
        }

        data = list
        diffUtilCallbackHelper!!.update(oldList, list)
        DiffUtil.calculateDiff(diffUtilCallbackHelper, false)
            .dispatchUpdatesTo(this)
        onDataUpdatedCallback?.onDataUpdated(needScrollToTop)
    }

    class DiffUtilCallbackHelper<T : Any>(
        private var oldList: List<T> = emptyList(),
        private var newList: List<T> = emptyList(),
        private var itemCallback: ItemCallback<T>,
    ) : DiffUtil.Callback() {
        fun update(oldList: List<T>, newList: List<T>) {
            this.oldList = oldList
            this.newList = newList
        }

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            itemCallback.areItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            itemCallback.areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])
    }

    class Builder(
        private var onViewEvent: OnViewEvent<Playable>? = null,
        private var onItemBoundCallback: OnItemBoundCallback<Playable>? = null,
        private var onDataUpdatedCallback: OnDataUpdatedCallback? = null,
        private var itemCallback: ItemCallback<Playable>? = null,
    ) {
        fun setViewEvent(onViewEvent: OnViewEvent<Playable>) = apply {
            this.onViewEvent = onViewEvent
        }

        fun setOnItemBoundCB(onItemBoundCallback: OnItemBoundCallback<Playable>) = apply {
            this.onItemBoundCallback = onItemBoundCallback
        }

        fun setOnDataUpdatedCB(onDataUpdatedCallback: OnDataUpdatedCallback) = apply {
            this.onDataUpdatedCallback = onDataUpdatedCallback
        }

        fun setItemCallback(itemCallback: ItemCallback<Playable>) = apply {
            this.itemCallback = itemCallback
        }

        fun build() = NewPlayingAdapter(
            onViewEvent,
            onItemBoundCallback, onDataUpdatedCallback,
            itemCallback
        )
    }
}

class TouchHelper(
    private val onSwipedCB: OnSwipedCB,
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
    fun interface OnSwipedCB {
        fun onSwiped(position: Int, direction: Int)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.absoluteAdapterPosition
        onSwipedCB.onSwiped(position, direction)
    }
}

