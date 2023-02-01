package com.lalilu.lmusic

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.IndexFilter
import com.lalilu.lmedia.extension.LMediaExtFactory
import com.lalilu.lmusic.datastore.SettingsDataStore
import com.lalilu.lmusic.utils.EQHelper
import com.lalilu.lmusic.utils.StatusBarLyricExt
import com.lalilu.lmusic.utils.coil.CrossfadeTransitionFactory
import com.lalilu.lmusic.utils.coil.fetcher.AlbumCoverFetcher
import com.lalilu.lmusic.utils.coil.fetcher.SongCoverFetcher
import com.lalilu.lmusic.utils.coil.keyer.SongCoverKeyer
import dagger.hilt.android.HiltAndroidApp
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

    override fun newLMediaExt(): LMediaExtFactory.LMediaExt =
        LMediaExtFactory.LMediaExt.Builder()
            .setIndexFilter(object : IndexFilter {
                override fun onSongsBuilt(songs: List<LSong>): List<LSong> =
                    songs.filter {
                        if (settingsDataStore.run { enableUnknownFilter.get() } != true)
                            return@filter true

                        !it._artist.contains("<unknown>")
                    }
            })
            .build()

    override fun onCreate() {
        super.onCreate()
        StatusBarLyricExt.init(this)
        EQHelper.init(this)
    }
}