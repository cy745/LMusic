package com.lalilu.lmusic

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.blankj.utilcode.util.LogUtils
import com.lalilu.component.ComponentModule
import com.lalilu.lalbum.AlbumModule
import com.lalilu.lartist.ArtistModule
import com.lalilu.ldictionary.DictionaryModule
import com.lalilu.lhistory.HistoryModule
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.indexer.FilterGroup
import com.lalilu.lmedia.indexer.FilterProvider
import com.lalilu.lmusic.utils.extension.ignoreSSLVerification
import com.lalilu.lplayer.LPlayer
import com.lalilu.lplaylist.PlaylistModule
import com.zhangke.krouter.KRouter
import com.zhangke.krouter.generated.KRouterInjectMap
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import java.io.File

class LMusicApp : Application(), SingletonImageLoader.Factory, FilterProvider, ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
    private val imageLoader: ImageLoader by inject()
    private val filterGroup: FilterGroup by inject()

    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader
    override fun newFilterGroup(): FilterGroup = filterGroup

    override fun onCreate() {
        super.onCreate()

        KRouter.init(KRouterInjectMap::getMap)

        SingletonImageLoader
            .setSafe(this)

        LogUtils.getConfig()
            .setLog2FileSwitch(true)
            .setFileExtension(".log")
            .setSaveDays(7)
            .setStackDeep(3)
            .setDir(File("${cacheDir}/log"))

        ignoreSSLVerification()
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
                HistoryModule.module,
                ArtistModule,
                AlbumModule,
                DictionaryModule,
                LPlayer.module,
                LMedia.module
            )
        }
    }
}