package com.lalilu.lmusic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

const val VIEW_NORMAL = 0
const val VIEW_HEADER = 1
const val VIEW_FOOTER = 2

@IntDef(VIEW_NORMAL, VIEW_HEADER, VIEW_FOOTER)
@Retention(AnnotationRetention.SOURCE)
annotation class AdapterViewType

abstract class BaseAdapter<I : Any, B : ViewDataBinding> constructor(
    private val layoutId: Int
) : RecyclerView.Adapter<BaseAdapter<I, B>.BaseViewHolder>() {

    var offset: Int = 0
    var data: MutableList<I> = ArrayList()
    open var onItemClick: (item: I) -> Unit = {}
    open var onItemLongClick: (item: I) -> Unit = {}
    open val itemCallback: DiffUtil.ItemCallback<I>? = null

    abstract fun onBind(binding: B, item: I)

    inner class BaseViewHolder constructor(val binding: B) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                layoutId, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = holder.binding
        val offsetPosition = (position + offset) % data.size
        val item = data[offsetPosition]
        binding.root.setOnClickListener {
            onItemClick(item)
        }
        binding.root.setOnLongClickListener {
            onItemLongClick(item)
            return@setOnLongClickListener true
        }
        onBind(binding, item)
    }

    override fun getItemCount(): Int = data.size

    open fun setDiffNewData(list: MutableList<I>?) {
        val temp = list ?: ArrayList()
        itemCallback ?: return run { data = temp }

        val diffResult = DiffUtil.calculateDiff(Callback(this.data, temp, itemCallback!!))
        data = temp
        diffResult.dispatchUpdatesTo(this)
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