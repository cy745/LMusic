package com.lalilu.lmedia

import android.content.Context
import androidx.startup.Initializer
import com.lalilu.lmedia.extension.KanjiToHiraTransformer

class StartUp : Initializer<Unit> {

    override fun create(context: Context) {
        LMedia.init(context)
        KanjiToHiraTransformer.init(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }

    companion object {
        init {
            System.loadLibrary("taglib")
        }
    }
}