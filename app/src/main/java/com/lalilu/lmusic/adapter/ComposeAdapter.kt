package com.lalilu.lmusic.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.LMusicTheme
import com.lalilu.lmusic.screen.component.card.PlayingCard
import com.lalilu.lmusic.utils.extension.moveHeadToTail
import java.lang.ref.WeakReference

open class ComposeAdapter(
    private val onItemClick: (LSong) -> Unit = {},
    private val onItemLongClick: (LSong) -> Unit = {},
    onSwipeToLeft: onItemSwipedListener<LSong>,
    onSwipeToRight: onItemSwipedListener<LSong>
) : RecyclerView.Adapter<ComposeAdapter.ComposeViewHolder>() {

    inner class ComposeViewHolder(
        private val composeView: ComposeView
    ) : RecyclerView.ViewHolder(composeView) {

        fun bind(
            item: LSong,
            onItemClick: (LSong) -> Unit,
            onItemLongClick: (LSong) -> Unit
        ) {
            composeView.setContent {
                LMusicTheme {
                    PlayingCard(
                        getSong = { item },
                        loadDelay = { 50L },
                        onClick = { onItemClick(item) },
                        onLongClick = { onItemLongClick(item) }
                    )
                }
            }
        }
    }

    fun interface onItemSwipedListener<I> {
        fun onSwiped(item: I): Boolean
    }

    var data: MutableList<LSong> = ArrayList()
    open var mRecyclerView: WeakReference<RecyclerView>? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        itemDragCallback.let {
            ItemTouchHelper(it)
                .attachToRecyclerView(recyclerView)
        }
        mRecyclerView = WeakReference(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComposeViewHolder {
        return ComposeViewHolder(ComposeView(parent.context))
    }

    override fun onViewRecycled(holder: ComposeViewHolder) {
        (holder.itemView as ComposeView).disposeComposition()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ComposeViewHolder, position: Int) {

        holder.bind(
            item = data[position],
            onItemClick = onItemClick,
            onItemLongClick = onItemLongClick
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setNewData(list: MutableList<LSong>?) {
        val temp = list ?: java.util.ArrayList()
        val diffResult = DiffUtil.calculateDiff(Callback(this.data, temp, itemCallback))
        data = temp
        diffResult.dispatchUpdatesTo(this)
    }

    open val itemDragCallback = OnItemSwipeHandler(
        onSwipeToLeft = onSwipeToLeft,
        onSwipeToRight = onSwipeToRight
    )

    open val itemCallback: DiffUtil.ItemCallback<LSong> =
        object : DiffUtil.ItemCallback<LSong>() {
            override fun areItemsTheSame(oldItem: LSong, newItem: LSong): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: LSong, newItem: LSong): Boolean {
                return oldItem.id == newItem.id &&
                        oldItem.name == newItem.name &&
                        oldItem._artist == newItem._artist &&
                        oldItem.durationMs == newItem.durationMs
            }
        }

    inner class Callback(
        private val oldList: List<LSong>,
        private val newList: List<LSong>,
        private val itemCallback: DiffUtil.ItemCallback<LSong>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return itemCallback.areItemsTheSame(
                oldList[oldItemPosition],
                newList[newItemPosition]
            )
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return itemCallback.areContentsTheSame(
                oldList[oldItemPosition],
                newList[newItemPosition]
            )
        }
    }

    open inner class OnItemSwipeHandler(
        private val onSwipeToLeft: onItemSwipedListener<LSong>,
        private val onSwipeToRight: onItemSwipedListener<LSong>
    ) : ItemTouchHelper.Callback() {
        open val swipeFlags: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT

        override fun isItemViewSwipeEnabled(): Boolean = swipeFlags != 0
        override fun isLongPressDragEnabled(): Boolean = false

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int = makeMovementFlags(0, swipeFlags)

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.absoluteAdapterPosition
            val item = data[position]
            when (direction) {
                ItemTouchHelper.LEFT -> {
                    val result = onSwipeToLeft.onSwiped(item)
                    if (result) remove(position) else recover(position)
                }
                ItemTouchHelper.RIGHT -> {
                    remove(position)
                    onSwipeToRight.onSwiped(item)
                }
            }
        }

        fun remove(position: Int) {
            data.removeAt(position)
            notifyItemRemoved(position)
        }

        fun recover(position: Int) {
            notifyItemChanged(position)
        }
    }
}


fun ComposeAdapter.setDiffNewData(list: MutableList<LSong>?) {
    val recyclerView = mRecyclerView?.get() ?: run {
        this.setNewData(list)
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
