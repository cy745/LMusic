package com.lalilu.lplayer

import android.content.Context
import androidx.startup.Initializer
import com.lalilu.lmedia.LMedia


class Startup : Initializer<Unit> {
    override fun create(context: Context) {
        LMedia.whenReady {
            MPlayer.init()
        }
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}