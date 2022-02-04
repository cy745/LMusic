package com.lalilu.lmusic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView


abstract class BaseAdapter<I : Any, B : ViewDataBinding> constructor(
    private val layoutId: Int
) : RecyclerView.Adapter<BaseAdapter<I, B>.BaseViewHolder>() {

    var data: MutableList<I> = ArrayList()
    open var onItemClickListener: (item: I) -> Unit = {}
    open var onItemLongClickListener: (item: I) -> Unit = {}
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
        val item = data[position]
        binding.root.setOnClickListener {
            onItemClickListener(item)
        }
        binding.root.setOnLongClickListener {
            onItemLongClickListener(item)
            return@setOnLongClickListener true
        }
        onBind(binding, item)
    }

    override fun getItemCount(): Int = data.size

    fun setDiffNewData(list: MutableList<I>?) {
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