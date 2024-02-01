package com.lalilu.lmusic

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.lalilu.component.ComponentModule
import com.lalilu.lalbum.AlbumModule
import com.lalilu.lartist.ArtistModule
import com.lalilu.ldictionary.DictionaryModule
import com.lalilu.lhistory.HistoryModule
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.indexer.FilterGroup
import com.lalilu.lmedia.indexer.FilterProvider
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.utils.extension.ignoreSSLVerification
import com.lalilu.lplayer.LPlayer
import com.lalilu.lplaylist.PlaylistModule
import com.umeng.commonsdk.UMConfigure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import kotlin.coroutines.CoroutineContext

class LMusicApp : Application(), CoroutineScope, ImageLoaderFactory, FilterProvider,
    ViewModelStoreOwner {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    override val viewModelStore: ViewModelStore = ViewModelStore()
    private val imageLoader: ImageLoader by inject()
    private val filterGroup: FilterGroup by inject()
    private val settingsSp: SettingsSp by inject()

    override fun newImageLoader(): ImageLoader = imageLoader
    override fun newFilterGroup(): FilterGroup = filterGroup

    override fun onCreate() {
        super.onCreate()
        UMConfigure.preInit(this, "<appkey>", "LMusic")

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
                HistoryModule,
                ArtistModule,
                AlbumModule,
                DictionaryModule,
                LPlayer.module,
                LMedia.module
            )
        }

        if (settingsSp.isGuidingOver.value) {
            doInitUmeng()
        }
    }

    fun doInitUmeng() {
        launch {
            UMConfigure.init(
                this@LMusicApp,
                "<appkey>",
                "LMusic",
                UMConfigure.DEVICE_TYPE_PHONE,
                ""
            )
        }
    }
}