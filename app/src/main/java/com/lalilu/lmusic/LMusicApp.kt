package com.lalilu.lmusic

import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.hjq.permissions.XXPermissions
import com.lalilu.lmusic.base.BaseApplication
import com.lalilu.lmusic.service.LMusicPlayerModule
import com.lalilu.lmusic.utils.SharedPreferenceModule
import com.lalilu.media.LMusicMediaModule

class LMusicApp : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        XXPermissions.setScopedStorage(true)
        LMusicPlayerModule.getInstance(this)
        LMusicMediaModule.getInstance(this)
        SharedPreferenceModule.getInstance(this)
        val config = ImagePipelineConfig.newBuilder(this)
            .setDownsampleEnabled(true)
            .build()
        Fresco.initialize(this, config)
    }
}