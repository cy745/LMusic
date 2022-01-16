package com.lalilu.lmusic.utils

import android.view.View

object TextUtils {
    fun durationToString(duration: Number): String {
        var temp = duration.toLong().div(1000)
        if (temp < 1) {
            temp *= 1000
        }

        val min = temp.div(60)
        val sec = temp % 60
        return "${if (min < 10) "0" else ""}$min:${if (sec < 10) "0" else ""}$sec"
    }

    fun setVisibility(boolean: Boolean): Int {
        return if (boolean) View.VISIBLE else View.GONE
    }
}