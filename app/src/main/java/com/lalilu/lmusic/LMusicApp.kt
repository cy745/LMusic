package com.lalilu.lmusic

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
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
class LMusicApp : Application(), ImageLoaderFactory {

    @Inject
    lateinit var callFactory: OkHttpClient

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

    override fun onCreate() {
        super.onCreate()
        StatusBarLyricExt.init(this)
        EQHelper.init(this)
    }
}