package com.lalilu.lplayer

import android.content.Context
import androidx.startup.Initializer


class Startup : Initializer<Unit> {
    override fun create(context: Context) {
        MPlayer.init()
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}