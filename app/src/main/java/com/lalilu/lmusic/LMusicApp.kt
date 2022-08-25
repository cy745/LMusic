package com.lalilu.lmusic

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.lalilu.lmusic.manager.SpManager
import com.lalilu.lmusic.utils.fetcher.AlbumCoverFetcher
import com.lalilu.lmusic.utils.fetcher.SongCoverFetcher
import dagger.hilt.android.HiltAndroidApp
import okhttp3.Call
import javax.inject.Inject

@HiltAndroidApp
class LMusicApp : Application(), ImageLoaderFactory {

    @Inject
    lateinit var callFactory: Call.Factory

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .callFactory(callFactory)
            .components {
                add(AlbumCoverFetcher.AlbumFactory(callFactory))
                add(SongCoverFetcher.SongFactory(callFactory))
            }.build()

    override fun onCreate() {
        super.onCreate()
        SpManager.init(this)
    }
}