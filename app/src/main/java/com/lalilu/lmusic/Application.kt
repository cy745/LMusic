package com.lalilu.lmusic

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.lalilu.lmusic.database.MusicDatabase
import com.lalilu.lmusic.utils.NotificationUtils
import com.lalilu.lmusic.viewmodel.MusicDataBaseViewModel
import com.lalilu.lmusic.viewmodel.MusicServiceViewModel

class MusicApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        NotificationUtils.getInstance(this)
        MusicDatabase.getInstance(this)
        MusicDataBaseViewModel.getInstance(this)
        MusicServiceViewModel.getInstance()
        Fresco.initialize(this)
    }
}