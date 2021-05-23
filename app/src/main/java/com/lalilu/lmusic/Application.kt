package com.lalilu.lmusic

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.lalilu.lmusic.database.MusicDatabase
import com.lalilu.lmusic.viewmodel.MusicDataBaseViewModel

class MusicApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        MusicDatabase.getInstance(this)
        MusicDataBaseViewModel.getInstance(this)
        Fresco.initialize(this)
    }
}