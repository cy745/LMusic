package com.lalilu.lmusic

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.lalilu.media.LMusicMediaModule

class LMusicApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LMusicMediaModule.getInstance(this)
        Fresco.initialize(this)
    }
}