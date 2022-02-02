package com.lalilu.lmusic.ui

import android.graphics.Color
import android.os.Build
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat.SHOW_DIVIDER_NONE
import androidx.appcompat.widget.SearchView
import com.lalilu.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MySearchBar constructor(
    private val menuItem: MenuItem,
    private val update: (text: String?) -> Unit
) : SearchView.OnQueryTextListener, SearchView.OnCloseListener, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main

    val isExpended: Boolean
        get() {
            return menuItem.isActionViewExpanded
        }

    fun expend() = launch {
        if (isExpended) return@launch
        menuItem.expandActionView()
    }

    fun collapse() = launch {
        if (!isExpended) return@launch
        menuItem.collapseActionView()
    }

    init {
        val searchView = menuItem.actionView as SearchView
        val mUnderline = searchView.findViewById<View>(R.id.search_plate)
        mUnderline.setBackgroundColor(Color.argb(0, 255, 255, 255))

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