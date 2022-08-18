package com.lalilu.lmusic

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.lalilu.lmusic.utils.fetcher.EmbeddedLyricFetcher
import com.lalilu.lmusic.utils.fetcher.MSongCoverFetcher
import dagger.hilt.android.HiltAndroidApp
import okhttp3.Call
import javax.inject.Inject

@HiltAndroidApp
class LMusicApp : Application(), ImageLoaderFactory {

    @Inject
    lateinit var coverFetcher: MSongCoverFetcher

    @Inject
    lateinit var lyricFetcher: EmbeddedLyricFetcher

    @Inject
    lateinit var callFactory: Call.Factory

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .callFactory(callFactory)
            .componentRegistry {
                add(coverFetcher)
                add(lyricFetcher)
            }.build()

    override fun onCreate() {
        super.onCreate()
//        SpiderMan.setTheme(R.style.SpiderManTheme_Dark)
//        SpManager.init(this)
    }
}