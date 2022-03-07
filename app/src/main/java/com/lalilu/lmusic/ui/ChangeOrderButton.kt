package com.lalilu.lmusic.ui

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton

class ChangeOrderButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : MaterialButton(context, attrs) {
    private val buttonWithCallback: MutableList<Pair<Int, String>> = ArrayList()
    var currentIndex = 0
        set(value) {
            if (value >= buttonWithCallback.size) return
            setIconResource(buttonWithCallback[value].first)
            field = value
        }

    private fun changeToNextState() {
        if (buttonWithCallback.isEmpty()) return
        currentIndex = (currentIndex + 1) % buttonWithCallback.size
    }

    override fun callOnClick(): Boolean {
        changeToNextState()
        return super.callOnClick()
    }
}