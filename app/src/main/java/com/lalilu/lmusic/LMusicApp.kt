package com.lalilu.lmusic

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.lalilu.lmusic.manager.SpManager
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.utils.fetcher.AlbumCoverFetcher
import com.lalilu.lmusic.utils.fetcher.SongCoverFetcher
import com.lalilu.lmusic.utils.sources.LyricSourceFactory
import dagger.hilt.android.HiltAndroidApp
import okhttp3.Call
import javax.inject.Inject

@HiltAndroidApp
class LMusicApp : Application(), ImageLoaderFactory {

    @Inject
    lateinit var callFactory: Call.Factory

    @Inject
    lateinit var lyricSourceFactory: LyricSourceFactory

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .callFactory(callFactory)
            .components {
                add(AlbumCoverFetcher.AlbumFactory())
                add(SongCoverFetcher.SongFactory(callFactory))
            }.build()

    override fun onCreate() {
        super.onCreate()
//        BlockCanary.install(this, object : BlockCanaryContext() {
//            override fun provideQualifier(): String = "BlockCanary"
//            override fun provideUid(): String = "uid_block_canary"
//            override fun provideBlockThreshold(): Int = 50
//            override fun providePath(): String {
//                return "/data/com.lalilu.lmusic.debug/files/logs/"
//            }
//        }).start()
        LMusicBrowser.init(this)
        SpManager.init(this)
    }
}