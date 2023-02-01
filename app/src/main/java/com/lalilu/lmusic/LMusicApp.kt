package com.lalilu.lmusic

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.extension.LMediaExt
import com.lalilu.lmedia.extension.LMediaExtFactory
import com.lalilu.lmedia.extension.LMediaLifeCycle
import com.lalilu.lmusic.datastore.SettingsDataStore
import com.lalilu.lmusic.utils.EQHelper
import com.lalilu.lmusic.utils.StatusBarLyricExt
import com.lalilu.lmusic.utils.coil.CrossfadeTransitionFactory
import com.lalilu.lmusic.utils.coil.fetcher.AlbumCoverFetcher
import com.lalilu.lmusic.utils.coil.fetcher.SongCoverFetcher
import com.lalilu.lmusic.utils.coil.keyer.SongCoverKeyer
import com.lalilu.lmusic.utils.filter.DictionaryFilter
import com.lalilu.lmusic.utils.filter.UnknownFilter
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class LMusicApp : Application(), ImageLoaderFactory, LMediaExtFactory {

    @Inject
    lateinit var callFactory: OkHttpClient

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .callFactory(callFactory)
            .components {
                add(AlbumCoverFetcher.AlbumFactory())
                add(SongCoverFetcher.SongFactory(callFactory))
                add(SongCoverKeyer())
            }
            .transitionFactory(CrossfadeTransitionFactory())
            .build()

    override fun newLMediaExt(): LMediaExt =
        LMediaExt.Builder()
            .addIndexFilter(UnknownFilter { settingsDataStore })
            .addIndexFilter(DictionaryFilter { settingsDataStore })
            .setLifeCycleListener(object : LMediaLifeCycle.Listener {
                override fun onFirstIndexFinish() {
                    settingsDataStore.apply {
                        enableUnknownFilter.flow()
                            .distinctUntilChanged()
                            .onEach { LMedia.index() }
                            .launchIn(this)
                    }
                }
            })
            .build()

    override fun onCreate() {
        super.onCreate()
        StatusBarLyricExt.init(this)
        EQHelper.init(this)
    }
}