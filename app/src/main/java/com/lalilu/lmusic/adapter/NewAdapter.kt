package com.lalilu.lmusic.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.R
import com.lalilu.databinding.ItemPlayingBinding
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.utils.extension.moveHeadToTail
import com.lalilu.lmusic.utils.extension.removeAt

abstract class NewAdapter<I : Any, B : ViewDataBinding> constructor(
    private val layoutId: Int
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewViewHolder {
        return NewViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                layoutId, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: NewAdapter<I, B>.NewViewHolder, position: Int) {
        val binding = holder.binding
        val item = data[position]
        binding.root.tag = getIdFromItem(item)
        binding.root.setOnClickListener(this)
        binding.root.setOnLongClickListener(this)
        onBind(binding, item, position)
    }
}

class NewPlayingAdapter private constructor(
    private val onClickCallback: OnClickCallback?,
    private val onLongClickCallback: OnLongClickCallback?,
    private val onSwipedLeftCallback: OnSwipedLeftCallback?,
    private val onSwipedRightCallback: OnSwipedRightCallback?,
    private val onDataUpdatedCallback: OnDataUpdatedCallback?,
    private val onItemBoundCallback: OnItemBoundCallback?,
    private val itemCallback: ItemCallback<LSong>?
) : NewAdapter<LSong, ItemPlayingBinding>(R.layout.item_playing) {

    private val diffUtilCallbackHelper = itemCallback?.let {
        DiffUtilCallbackHelper(itemCallback = it)
    }
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
            ItemTouchHelper.LEFT -> onSwipedLeftCallback?.onSwipedLeft(item)
            ItemTouchHelper.RIGHT -> onSwipedRightCallback?.onSwipedRight(item)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        ItemTouchHelper(touchHelper).attachToRecyclerView(recyclerView)
    }

    fun interface OnClickCallback {
        fun onClick(id: String)
    }

    fun interface OnLongClickCallback {
        fun onLongClick(id: String)
    }

    fun interface OnSwipedLeftCallback {
        fun onSwipedLeft(item: LSong): Boolean
    }

    fun interface OnSwipedRightCallback {
        fun onSwipedRight(item: LSong): Boolean
    }

    fun interface OnDataUpdatedCallback {
        fun onDataUpdated(needScrollToTop: Boolean)
    }

    fun interface OnItemBoundCallback {
        fun onItemBound(binding: ItemPlayingBinding, item: LSong)
    }

    override fun onBind(binding: ItemPlayingBinding, item: LSong, position: Int) {
        binding.song = item
        onItemBoundCallback?.onItemBound(binding, item)
    }

    override fun getIdFromItem(item: LSong): String {
        return item.id
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onClick(v: View?) {
        onClickCallback?.onClick(v?.tag.toString())
    }

    override fun onLongClick(v: View?): Boolean {
        onLongClickCallback?.onLongClick(v?.tag.toString())
        return true
    }

    fun setDiffData(list: List<LSong>) {
        var needScrollToTop = false
        if (itemCallback == null || diffUtilCallbackHelper == null) {
            data = list
            notifyDataSetChanged()
            onDataUpdatedCallback?.onDataUpdated(false)
        }

        var oldList = data
        if (list.isNotEmpty() && oldList.isNotEmpty()) {
            // 排除播放上一首的情况
            if (oldList.lastOrNull()?.id == list[0].id) {
                needScrollToTop = true
            } else {
                // 预先将头部部分差异进行转移
                // 通过比对第一个元素的id来判断是否需要转移
                val size = oldList.indexOfFirst { it.id == list[0].id }
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
        private var itemCallback: ItemCallback<T>
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
        private var onClickCallback: OnClickCallback? = null,
        private var onLongClickCallback: OnLongClickCallback? = null,
        private var onSwipedLeftCallback: OnSwipedLeftCallback? = null,
        private var onSwipedRightCallback: OnSwipedRightCallback? = null,
        private var onDataUpdatedCallback: OnDataUpdatedCallback? = null,
        private var onItemBoundCallback: OnItemBoundCallback? = null,
        private var itemCallback: ItemCallback<LSong>? = null
    ) {
        fun setOnClickCB(onClickCallback: OnClickCallback) = apply {
            this.onClickCallback = onClickCallback
        }

        fun setOnLongClickCB(onLongClickCallback: OnLongClickCallback) = apply {
            this.onLongClickCallback = onLongClickCallback
        }

        fun setOnSwipedLeftCB(onSwipedLeftCallback: OnSwipedLeftCallback) = apply {
            this.onSwipedLeftCallback = onSwipedLeftCallback
        }

        fun setOnSwipedRightCB(onSwipedRightCallback: OnSwipedRightCallback) = apply {
            this.onSwipedRightCallback = onSwipedRightCallback
        }

        fun setOnDataUpdatedCB(onDataUpdatedCallback: OnDataUpdatedCallback) = apply {
            this.onDataUpdatedCallback = onDataUpdatedCallback
        }

        fun setOnItemBoundCB(onItemBoundCallback: OnItemBoundCallback) = apply {
            this.onItemBoundCallback = onItemBoundCallback
        }

        fun setItemCallback(itemCallback: ItemCallback<LSong>) = apply {
            this.itemCallback = itemCallback
        }

        fun build() = NewPlayingAdapter(
            onClickCallback,
            onLongClickCallback,
            onSwipedLeftCallback,
            onSwipedRightCallback,
            onDataUpdatedCallback,
            onItemBoundCallback,
            itemCallback
        )
    }
}

class TouchHelper(
    private val onSwipedCB: OnSwipedCB
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
    fun interface OnSwipedCB {
        fun onSwiped(position: Int, direction: Int)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.absoluteAdapterPosition
        onSwipedCB.onSwiped(position, direction)
    }
}

