package com.lalilu.lmusic

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.lalilu.media.LMusicMediaContainer

class LMusicApplication : Application() {
//    lateinit var audioMediaScanner: AudioMediaScanner

    override fun onCreate() {
        super.onCreate()
        LMusicMediaContainer.getInstance(this)

//        audioMediaScanner = AudioMediaScanner(this)
//        MusicDatabase.getInstance(this)
//        MusicDataBaseViewModel.getInstance(this)
//        MusicServiceViewModel.getInstance()
        Fresco.initialize(this)
    }
}