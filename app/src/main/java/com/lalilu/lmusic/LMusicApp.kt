package com.lalilu.lmusic

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.lalilu.lmusic.datasource.MDataBase
import com.lalilu.lmusic.repository.HistoryDataStore
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.service.LMusicLyricManager
import com.lalilu.lmusic.service.LMusicRuntime
import com.lalilu.lmusic.utils.StatusBarLyricExt
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

    @Inject
    lateinit var historyDataStore: HistoryDataStore

    @Inject
    lateinit var mDataBase: MDataBase

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .callFactory(callFactory)
            .components {
                add(AlbumCoverFetcher.AlbumFactory())
                add(SongCoverFetcher.SongFactory(callFactory))
            }.build()

    override fun onCreate() {
        super.onCreate()
//        Indexer.init { this }
        StatusBarLyricExt.init(this)
        LMusicLyricManager.init(lyricSourceFactory)
        LMusicRuntime.init(historyDataStore, mDataBase)
        LMusicBrowser.init(this, historyDataStore)
    }
}