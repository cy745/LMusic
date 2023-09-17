package com.lalilu.lmusic

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.lalilu.lmedia.LMedia
import com.lalilu.lplayer.LPlayer
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LMusicApp : Application(), ImageLoaderFactory {

    private val imageLoader: ImageLoader by inject()

    override fun newImageLoader(): ImageLoader = imageLoader

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LMusicApp)
            modules(
                AppModule,
                ApiModule,
                ViewModelModule,
                DatabaseModule,
                RuntimeModule,
                LPlayer.module,
                LMedia.module
            )
        }
    }
}