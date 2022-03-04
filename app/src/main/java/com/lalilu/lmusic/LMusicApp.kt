package com.lalilu.lmusic

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.lalilu.R
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.utils.fetcher.EmbeddedCoverFetcher
import com.lalilu.lmusic.utils.fetcher.EmbeddedLyricFetchers
import com.simple.spiderman.SpiderMan
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@HiltAndroidApp
class LMusicApp : Application(), ImageLoaderFactory, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Inject
    lateinit var lyricFetchers: EmbeddedLyricFetchers

    @Inject
    lateinit var coverFetcher: EmbeddedCoverFetcher

    @Inject
    lateinit var mediaSource: BaseMediaSource

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .componentRegistry {
                add(coverFetcher)
                add(lyricFetchers)
            }.build()

    override fun onCreate() {
        super.onCreate()
        SpiderMan.setTheme(R.style.SpiderManTheme_Dark)
    }
}