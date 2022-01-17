package com.lalilu.lmusic.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridItemDecoration constructor(
    var gap: Int = 10,
    var spanCount: Int = 2,
    var edgeSnap: Boolean = true
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val pos = parent.getChildAdapterPosition(view)
        val column = (pos % spanCount)
        outRect.left = 0
        outRect.right = 0
        outRect.top = gap / 2
        outRect.bottom = gap / 2

        if (edgeSnap) {
            val temp = (spanCount - 1) * gap / spanCount
            when (column) {
                0 -> outRect.right = temp
                spanCount - 1 -> outRect.left = temp
                else -> {
                    outRect.left = temp / 2
                    outRect.right = temp / 2
                }
            }
            if (pos / spanCount == 0) outRect.top = 0
        } else {
            when (column) {
                0 -> {
                    outRect.left = gap
                    outRect.right = gap / 2
                }
                spanCount - 1 -> {
                    outRect.left = gap / 2
                    outRect.right = gap
                }
                else -> {
                    outRect.left = gap / 2
                    outRect.right = gap / 2
                }
            }
            if (pos / spanCount == 0) outRect.top = gap
        }
    }
}