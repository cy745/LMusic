package com.lalilu.lmusic

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.indexer.FilterGroup
import com.lalilu.lmedia.indexer.FilterProvider
import com.lalilu.lplayer.LPlayer
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LMusicApp : Application(), ImageLoaderFactory, FilterProvider, ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
    private val imageLoader: ImageLoader by inject()
    private val filterGroup: FilterGroup by inject()

    override fun newImageLoader(): ImageLoader = imageLoader
    override fun newFilterGroup(): FilterGroup = filterGroup

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
                FilterModule,
                LPlayer.module,
                LMedia.module
            )
        }
    }
}