package com.lalilu.lmusic.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.media3.common.MediaItem
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.lmedia.extension.getDuration
import com.lalilu.lmusic.LMusicTheme
import com.lalilu.lmusic.screen.component.card.PlayingCard
import com.lalilu.lmusic.utils.moveHeadToTail
import java.lang.ref.WeakReference

open class ComposeAdapter(
    private val onItemClick: (MediaItem) -> Unit = {},
    private val onItemLongClick: (MediaItem) -> Unit = {},
    private val onSwipeToLeft: onItemSwipedListener<MediaItem>,
    private val onSwipeToRight: onItemSwipedListener<MediaItem>
) : RecyclerView.Adapter<ComposeAdapter.ComposeViewHolder>() {
    inner class ComposeViewHolder(
        private val composeView: ComposeView
    ) : RecyclerView.ViewHolder(composeView) {
        init {
            composeView.setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
        }

        fun bind(
            mediaItem: MediaItem,
            onItemClick: (MediaItem) -> Unit,
            onItemLongClick: (MediaItem) -> Unit
        ) {
            composeView.setContent {
                LMusicTheme {
                    PlayingCard(
                        mediaItem = mediaItem,
                        onClick = { onItemClick(mediaItem) },
                        onLongClick = { onItemLongClick(mediaItem) }
                    )
                }
            }
        }
    }

    fun interface onItemSwipedListener<I> {
        fun onSwiped(item: I): Boolean
    }

    var data: MutableList<MediaItem> = ArrayList()
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
            mediaItem = data[position],
            onItemClick = onItemClick,
            onItemLongClick = onItemLongClick
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setNewData(list: MutableList<MediaItem>?) {
        val temp = list ?: java.util.ArrayList()
        val diffResult = DiffUtil.calculateDiff(Callback(this.data, temp, itemCallback))
        data = temp
        diffResult.dispatchUpdatesTo(this)
    }

    open val itemDragCallback = OnItemSwipeHandler(
        onSwipeToLeft = onSwipeToLeft,
        onSwipeToRight = onSwipeToRight
    )

    open val itemCallback: DiffUtil.ItemCallback<MediaItem> =
        object : DiffUtil.ItemCallback<MediaItem>() {
            override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.mediaId == newItem.mediaId
            }

            override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.mediaId == newItem.mediaId &&
                        oldItem.mediaMetadata.title == newItem.mediaMetadata.title &&
                        oldItem.mediaMetadata.getDuration() == newItem.mediaMetadata.getDuration()
            }
        }

    inner class Callback(
        private val oldList: List<MediaItem>,
        private val newList: List<MediaItem>,
        private val itemCallback: DiffUtil.ItemCallback<MediaItem>
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
        private val onSwipeToLeft: onItemSwipedListener<MediaItem>,
        private val onSwipeToRight: onItemSwipedListener<MediaItem>
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


fun ComposeAdapter.setDiffNewData(list: MutableList<MediaItem>?) {
    val recyclerView = mRecyclerView?.get() ?: run {
        this.setNewData(list)
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
