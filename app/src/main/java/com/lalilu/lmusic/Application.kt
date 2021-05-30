package com.lalilu.lmusic

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.lalilu.lmusic.database.MusicDatabase
import com.lalilu.lmusic.notification.NotificationUtils
import com.lalilu.lmusic.utils.AudioMediaScanner
import com.lalilu.lmusic.viewmodel.MusicDataBaseViewModel
import com.lalilu.lmusic.viewmodel.MusicServiceViewModel

class MusicApplication : Application() {
    lateinit var audioMediaScanner: AudioMediaScanner

    override fun onCreate() {
        super.onCreate()

        audioMediaScanner = AudioMediaScanner(this)
        NotificationUtils.getInstance(this)
        MusicDatabase.getInstance(this)
        MusicDataBaseViewModel.getInstance(this)
        MusicServiceViewModel.getInstance()
        Fresco.initialize(this)
    }
}