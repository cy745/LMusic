package com.lalilu.lmusic

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.lalilu.R
import com.lalilu.lmusic.manager.KanjiToHiraTransformer
import com.lalilu.lmusic.manager.SpManager
import com.lalilu.lmusic.utils.fetcher.EmbeddedCoverFetcher
import com.lalilu.lmusic.utils.fetcher.EmbeddedLyricFetchers
import com.simple.spiderman.SpiderMan
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LMusicApp : Application(), ImageLoaderFactory {

    @Inject
    lateinit var lyricFetchers: EmbeddedLyricFetchers

    @Inject
    lateinit var coverFetcher: EmbeddedCoverFetcher

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .componentRegistry {
                add(coverFetcher)
                add(lyricFetchers)
            }.build()

    override fun onCreate() {
        super.onCreate()
        SpiderMan.setTheme(R.style.SpiderManTheme_Dark)
        SpManager.init(this)
        KanjiToHiraTransformer.init(this)
    }
}