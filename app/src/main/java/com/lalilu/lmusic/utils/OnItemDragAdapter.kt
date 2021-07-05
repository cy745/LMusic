package com.lalilu.lmusic.utils

import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.listener.OnItemDragListener

open class OnItemDragAdapter : OnItemDragListener {
    override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {}

    override fun onItemDragMoving(
        source: RecyclerView.ViewHolder?,
        from: Int,
        target: RecyclerView.ViewHolder?,
        to: Int
    ) {
    }

    override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder?, pos: Int) {}
}