package com.lalilu.lmusic

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.lalilu.component.ComponentModule
import com.lalilu.extension_core.ExtensionManager
import com.lalilu.lhistory.HistoryModule
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.indexer.FilterGroup
import com.lalilu.lmedia.indexer.FilterProvider
import com.lalilu.lmusic.utils.extension.ignoreSSLVerification
import com.lalilu.lplayer.LPlayer
import com.lalilu.lplaylist.PlaylistModule
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
        ignoreSSLVerification()
        ExtensionManager.loadExtensions(this)
        startKoin {
            androidContext(this@LMusicApp)
            modules(
                AppModule,
                ApiModule,
                ViewModelModule,
                RuntimeModule,
                FilterModule,
                PlaylistModule,
                ComponentModule,
                HistoryModule,
                LPlayer.module,
                LMedia.module
            )
        }

        // 插件端可能会比宿主更快使用ImageLoader，而插件端的context无法用于创建ImageLoader会导致闪退，
        // 故宿主端提前初始化ImageLoader避免插件端进行初始化
        Coil.imageLoader(this)
    }
}