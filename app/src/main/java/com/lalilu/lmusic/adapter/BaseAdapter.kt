package com.lalilu.lmusic.adapter

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference
import java.util.*

abstract class BaseAdapter<I : Any, B : ViewDataBinding> constructor(
    private val layoutId: Int
) : RecyclerView.Adapter<BaseAdapter<I, B>.BaseViewHolder>(),
    View.OnClickListener,
    View.OnLongClickListener {

    var data: MutableList<I> = ArrayList()
    open var onItemClick: (id: String, position: Int) -> Unit = { _, _ -> }
    open var onItemLongClick: (id: String, position: Int) -> Unit = { _, _ -> }
    open val itemCallback: DiffUtil.ItemCallback<I>? = null
    open val itemDragCallback: OnItemTouchCallbackAdapter? = null
    open var mRecyclerView: WeakReference<RecyclerView>? = null

    abstract fun onBind(binding: B, item: I, position: Int)
    abstract fun getIdFromItem(item: I): String

    inner class BaseViewHolder constructor(val binding: B) :
        RecyclerView.ViewHolder(binding.root)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        itemDragCallback?.let { ItemTouchHelper(it).attachToRecyclerView(recyclerView) }
        mRecyclerView = WeakReference(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                layoutId, parent, false
            )
        )
    }

    override fun onClick(v: View) {
        onItemClick(v.tag as String, 0)
    }

    override fun onLongClick(v: View): Boolean {
        onItemLongClick(v.tag as String, 0)
        return true
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = holder.binding
        val item = data[position]
        binding.root.tag = getIdFromItem(item)
        binding.root.setOnClickListener(this)
        binding.root.setOnLongClickListener(this)
        onBind(binding, item, position)
    }

    override fun getItemCount(): Int = data.size

    open fun setDiffNewData(list: MutableList<I>?) {
        val temp = list ?: ArrayList()
        itemCallback ?: return run {
            data = temp
            notifyDataSetChanged()
        }

        val diffResult = DiffUtil.calculateDiff(Callback(this.data, temp, itemCallback!!))
        data = temp
        diffResult.dispatchUpdatesTo(this)
    }

    abstract inner class OnItemTouchCallbackAdapter : ItemTouchHelper.Callback() {
        open val swipeFlags: Int = 0
        open val dragFlags: Int = 0

        open fun onSwiped(item: I, direction: Int, position: Int) {}
        open fun onMove(item: I, from: Int, to: Int): Boolean = false

        override fun isItemViewSwipeEnabled(): Boolean = swipeFlags != 0
        override fun isLongPressDragEnabled(): Boolean = dragFlags != 0

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeMovementFlags(dragFlags, swipeFlags)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            Collections.swap(
                data,
                viewHolder.absoluteAdapterPosition,
                target.absoluteAdapterPosition
            )
            notifyItemMoved(
                viewHolder.absoluteAdapterPosition,
                target.absoluteAdapterPosition
            )
            return onMove(
                data[viewHolder.absoluteAdapterPosition],
                viewHolder.absoluteAdapterPosition,
                target.absoluteAdapterPosition
            )
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.absoluteAdapterPosition
            onSwiped(data[position], direction, position)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            // TODO: 添加阻尼效果、松手振动
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        fun remove(position: Int) {
            data.removeAt(position)
            notifyItemRemoved(position)
        }

        fun recover(position: Int) {
            notifyItemChanged(position)
        }
    }

    inner class Callback<Item : I>(
        private val oldList: List<Item>,
        private val newList: List<Item>,
        private val itemCallback: DiffUtil.ItemCallback<Item>
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
}