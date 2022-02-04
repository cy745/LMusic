package com.lalilu.lmusic

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.hjq.permissions.XXPermissions
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
        XXPermissions.setScopedStorage(true)
    }
}