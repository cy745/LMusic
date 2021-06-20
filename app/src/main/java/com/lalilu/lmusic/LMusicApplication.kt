package com.lalilu.lmusic

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.lalilu.lmusic.fragment.LMusicViewModel
import com.lalilu.media.LMusicMediaModule

class LMusicApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LMusicMediaModule.getInstance(this)
        LMusicViewModel.getInstance(this)
        LMusicPlayListManager.getInstance(this)
        Fresco.initialize(this)
    }
}