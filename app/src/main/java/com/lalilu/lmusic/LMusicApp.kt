package com.lalilu.lmusic

import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.hjq.permissions.XXPermissions
import com.lalilu.lmusic.base.BaseApplication
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LMusicApp : BaseApplication() {

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        XXPermissions.setScopedStorage(true)

        val config = ImagePipelineConfig.newBuilder(this)
            .setDownsampleEnabled(true)
            .build()
        Fresco.initialize(this, config)
    }
}