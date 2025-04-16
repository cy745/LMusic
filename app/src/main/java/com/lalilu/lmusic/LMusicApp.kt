package com.lalilu.lmusic

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import coil3.SingletonImageLoader
import com.blankj.utilcode.util.LogUtils
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.util.Logger.Level
import com.lalilu.lalbum.AlbumModule
import com.lalilu.lartist.ArtistModule
import com.lalilu.lfolder.FolderModule
import com.lalilu.lhistory.HistoryModule
import com.lalilu.lmedia.LMedia
import com.lalilu.lmusic.utils.extension.ignoreSSLVerification
import com.lalilu.lmusic.utils.sketch.SongCoverFetcher
import com.lalilu.lplayer.MPlayer
import com.lalilu.lplaylist.PlaylistModule
import com.zhangke.krouter.KRouter
import com.zhangke.krouter.generated.KRouterInjectMap
import org.koin.android.ext.koin.androidContext
import org.koin.androix.startup.KoinStartup
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.KoinConfiguration
import org.koin.java.KoinJavaComponent
import org.koin.ksp.generated.module
import java.io.File


@Suppress("OPT_IN_USAGE")
class LMusicApp : Application(), ViewModelStoreOwner, KoinStartup, SingletonSketch.Factory {
    override val viewModelStore: ViewModelStore = ViewModelStore()

    override fun createSketch(context: PlatformContext): Sketch {
        return Sketch.Builder(context).apply {
            components {
                logger(Level.Debug)
                addFetcher(SongCoverFetcher.Factory())
            }
        }.build()
    }

    @KoinExperimentalAPI
    override fun onKoinStartup(): KoinConfiguration = KoinConfiguration {
        androidContext(this@LMusicApp)
        modules(
            MainModule.module,
            AppModule,
            ApiModule,
            ViewModelModule,
            HistoryModule.module,
            PlaylistModule.module,
            ArtistModule.module,
            AlbumModule.module,
            FolderModule,
            LMedia.module,
            MPlayer.module,
        )

        SingletonImageLoader
            .setSafe(KoinJavaComponent.get(SingletonImageLoader.Factory::class.java))
    }

    init {
        KRouter.init(KRouterInjectMap::getMap)
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