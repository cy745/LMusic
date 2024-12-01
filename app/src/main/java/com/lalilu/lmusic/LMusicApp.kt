package com.lalilu.lmusic

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
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
import com.lalilu.lplayer.MPlayer
import com.lalilu.lplaylist.PlaylistModule
import com.lalilu.lplaylist.PlaylistModule2
import com.zhangke.krouter.KRouter
import com.zhangke.krouter.generated.KRouterInjectMap
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.androix.startup.KoinStartup.onKoinStartup
import org.koin.java.KoinJavaComponent
import org.koin.ksp.generated.module
import java.io.File


@Suppress("OPT_IN_USAGE")
class LMusicApp : Application(), FilterProvider, ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
    private val filterGroup: FilterGroup by inject()

    override fun newFilterGroup(): FilterGroup = filterGroup

    init {
        KRouter.init(KRouterInjectMap::getMap)

        onKoinStartup {
            androidContext(this@LMusicApp)
            modules(
                MainModule.module,
                AppModule,
                ApiModule,
                ViewModelModule,
                RuntimeModule,
                FilterModule,
                PlaylistModule,
                ComponentModule,
                HistoryModule.module,
                PlaylistModule2.module,
                ArtistModule.module,
                AlbumModule.module,
                DictionaryModule,
                LMedia.module,
                MPlayer.module,
            )

            SingletonImageLoader
                .setSafe(KoinJavaComponent.get(SingletonImageLoader.Factory::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()

        LogUtils.getConfig()
            .setLog2FileSwitch(true)
            .setFileExtension(".log")
            .setSaveDays(7)
            .setStackDeep(3)
            .setDir(File("${cacheDir}/log"))

        ignoreSSLVerification()
    }
}