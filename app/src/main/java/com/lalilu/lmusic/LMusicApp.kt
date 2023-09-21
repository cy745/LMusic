package com.lalilu.lmusic

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.blankj.utilcode.util.FileUtils
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.indexer.Filter
import com.lalilu.lmedia.indexer.FilterGroup
import com.lalilu.lmedia.indexer.FilterProvider
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lplayer.LPlayer
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LMusicApp : Application(), ImageLoaderFactory, FilterProvider {

    private val imageLoader: ImageLoader by inject()
    private val settingSp: SettingsSp by inject()

    override fun newImageLoader(): ImageLoader = imageLoader

    override fun filters(): FilterGroup {
        val unknownArtistFilter = Filter(
            flow = settingSp.enableUnknownFilter.flow(true),
            getter = LSong::_artist::get,
            ignoreRule = { flowValue, getterValue ->
                flowValue == true && getterValue == "<unknown>"
            }
        )
        val durationFilter = Filter(
            flow = settingSp.durationFilter.flow(true),
            getter = LSong::durationMs::get,
            ignoreRule = { flowValue, getterValue ->
                getterValue <= (flowValue ?: 15)
            }
        )
        val pathFilter = Filter(
            flow = settingSp.blockedPaths.flow(true),
            getter = LSong::pathStr::get,
            ignoreRule = { flowValue, getterValue ->
                if (flowValue.isNullOrEmpty()) return@Filter false

                val path = FileUtils.getDirName(getterValue)
                    ?.takeIf(String::isNotEmpty)
                    ?: "Unknown dir"
                path in flowValue
            }
        )
        return FilterGroup(unknownArtistFilter, durationFilter, pathFilter)
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LMusicApp)
            modules(
                AppModule,
                ApiModule,
                ViewModelModule,
                DatabaseModule,
                RuntimeModule,
                LPlayer.module,
                LMedia.module
            )
        }
    }
}