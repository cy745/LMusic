package com.lalilu.lmusic.ui

import android.graphics.Color
import android.os.Build
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat.SHOW_DIVIDER_NONE
import androidx.appcompat.widget.SearchView
import com.blankj.utilcode.util.ConvertUtils
import com.lalilu.R

class MySearchBar constructor(
    menuItem: MenuItem,
    private val update: (text: String?) -> Unit
) : SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    init {
        val searchView = menuItem.actionView as SearchView
        val searchSrcText = searchView.findViewById<TextView>(R.id.search_src_text)
        val mUnderline = searchView.findViewById<View>(R.id.search_plate)
        val closeButton = searchView.findViewById<ImageView>(R.id.search_close_btn)
        val searchEditFrame = searchView.findViewById<LinearLayout>(R.id.search_edit_frame)

        searchSrcText.setTextColor(Color.WHITE)
        closeButton.setColorFilter(Color.WHITE)

        mUnderline.setBackgroundColor(Color.argb(0, 255, 255, 255))
        (searchEditFrame.layoutParams as LinearLayout.LayoutParams)
            .apply {
                leftMargin = 0
                rightMargin = 0
            }

        closeButton.setPadding(ConvertUtils.dp2px(10f), 0, 0, 0)
        closeButton.background = null

        searchView.setHasTransientState(true)
        searchView.showDividers = SHOW_DIVIDER_NONE

        searchView.setOnQueryTextListener(this)
        searchView.setOnCloseListener(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            searchView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        update(newText)
        return false
    }

    override fun onClose(): Boolean {
        update(null)
        return false
    }
}