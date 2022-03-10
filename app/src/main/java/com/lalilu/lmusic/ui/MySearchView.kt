package com.lalilu.lmusic.ui

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import com.blankj.utilcode.util.ConvertUtils
import com.lalilu.R

fun MySearchView.bind(update: (text: String?) -> Unit) {
    val listener = object : SearchView.OnQueryTextListener, SearchView.OnCloseListener {
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
    setOnQueryTextListener(listener)
    setOnCloseListener(listener)
}

class MySearchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SearchView(context, attrs) {

    init {
        val searchSrcText = findViewById<TextView>(R.id.search_src_text)
        val mUnderline = findViewById<View>(R.id.search_plate)
        val closeButton = findViewById<ImageView>(R.id.search_close_btn)
        val searchEditFrame = findViewById<LinearLayout>(R.id.search_edit_frame)

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

        setHasTransientState(true)
        showDividers = SHOW_DIVIDER_NONE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        }
    }
}