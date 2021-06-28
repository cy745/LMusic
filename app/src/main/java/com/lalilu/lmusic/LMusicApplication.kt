package com.lalilu.lmusic

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.hjq.permissions.XXPermissions
import com.lalilu.lmusic.fragment.LMusicViewModel
import com.lalilu.media.LMusicMediaModule
import com.lalilu.player.LMusicPlayerModule

class LMusicApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        XXPermissions.setScopedStorage(true)
        LMusicMediaModule.getInstance(this)
        LMusicPlayerModule.getInstance(this)
        LMusicViewModel.getInstance(this)
        LMusicPlayListManager.getInstance(this)
        val config = ImagePipelineConfig.newBuilder(this)
            .setDownsampleEnabled(true)
            .build()
        Fresco.initialize(this, config)
    }
}