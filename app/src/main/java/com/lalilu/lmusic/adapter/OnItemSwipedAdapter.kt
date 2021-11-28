package com.lalilu.lmusic.adapter

import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.listener.OnItemSwipeListener

open class OnItemSwipedAdapter : OnItemSwipeListener {

    override fun onItemSwipeStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {}

    override fun clearView(viewHolder: RecyclerView.ViewHolder?, pos: Int) {}

    override fun onItemSwiped(viewHolder: RecyclerView.ViewHolder?, pos: Int) {}

    override fun onItemSwipeMoving(
        canvas: Canvas?,
        viewHolder: RecyclerView.ViewHolder?,
        dX: Float,
        dY: Float,
        isCurrentlyActive: Boolean
    ) {
    }
}