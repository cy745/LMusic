package com.lalilu.lmusic

import StatusBarLyric.API.StatusBarLyric
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModelStoreOwner
import coil.EventListener
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import com.lalilu.R
import com.lalilu.common.base.SourceType
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.component.viewmodel.IPlayingViewModel
import com.lalilu.lalbum.viewModel.AlbumsViewModel
import com.lalilu.lartist.viewModel.ArtistsViewModel
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.indexer.Filter
import com.lalilu.lmedia.indexer.FilterGroup
import com.lalilu.lmedia.repository.LSongFastEncoder
import com.lalilu.lmusic.Config.LRCSHARE_BASEURL
import com.lalilu.lmusic.api.lrcshare.LrcShareApi
import com.lalilu.lmusic.datastore.LastPlayedSp
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.datastore.TempSp
import com.lalilu.lmusic.repository.CoverRepository
import com.lalilu.lmusic.repository.LyricRepository
import com.lalilu.lmusic.service.LMusicNotifier
import com.lalilu.lmusic.service.LMusicServiceConnector
import com.lalilu.lmusic.utils.EQHelper
import com.lalilu.lmusic.utils.coil.CrossfadeTransitionFactory
import com.lalilu.lmusic.utils.coil.fetcher.LAlbumFetcher
import com.lalilu.lmusic.utils.coil.fetcher.LSongFetcher
import com.lalilu.lmusic.utils.coil.keyer.PlayableKeyer
import com.lalilu.lmusic.utils.coil.keyer.SongCoverKeyer
import com.lalilu.lmusic.utils.coil.mapper.LSongMapper
import com.lalilu.lmusic.utils.extension.toBitmap
import com.lalilu.lmusic.viewmodel.HistoryViewModel
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.SearchLyricViewModel
import com.lalilu.lmusic.viewmodel.SearchViewModel
import com.lalilu.lplayer.notification.Notifier
import com.lalilu.lplaylist.entity.LPlaylistFastEncoder
import io.fastkv.FastKV
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLDecoder

val AppModule = module {
    single<ViewModelStoreOwner> { androidApplication() as ViewModelStoreOwner }
    single<GlobalNavigator> { GlobalNavigatorImpl }

    single<FastKV> {
        FastKV.Builder(androidApplication(), "LMusic")
            .encoder(
                arrayOf(
                    LSongFastEncoder,
                    LPlaylistFastEncoder
                )
            )
            .build()
    }

    single { SettingsSp(androidApplication()) }
    single { LastPlayedSp(androidApplication()) }
    single { TempSp(androidApplication()) }
    single { EQHelper(androidApplication()) }
    single {
        StatusBarLyric(
            androidContext(),
            ContextCompat.getDrawable(androidContext(), R.mipmap.ic_launcher)?.toBitmap()
                ?.toDrawable(androidContext().resources),
            "com.lalilu.lmusic",
            false
        )
    }
    single {
        ImageLoader.Builder(androidApplication())
            .callFactory(get<OkHttpClient>())
            .components {
                add(SongCoverKeyer())
                add(PlayableKeyer())
                add(LSongMapper())
                add(LSongFetcher.SongFactory())
                add(LAlbumFetcher.AlbumFactory())
            }
            .transitionFactory(CrossfadeTransitionFactory())
            .error(R.drawable.ic_music_2_line_100dp)
            .eventListener(object : EventListener {
                override fun onError(request: ImageRequest, result: ErrorResult) {
//                    LogUtils.w("[ImageLoader]:onError", request.data, result.throwable)
                }

                override fun onCancel(request: ImageRequest) {
//                    LogUtils.w("[ImageLoader]:onCancel", request.data)
                }
            })
            .build()
    }
}

val ViewModelModule = module {
    viewModelOf(::PlayingViewModel)
    viewModel<IPlayingViewModel> { get<PlayingViewModel>() }
    viewModelOf(::SearchViewModel)
    viewModelOf(::AlbumsViewModel)
    viewModelOf(::ArtistsViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::SearchLyricViewModel)
    viewModelOf(::LibraryViewModel)
}

val RuntimeModule = module {
    singleOf(::LMusicNotifier)
    single<Notifier> { get<LMusicNotifier>() }
    singleOf(::LMusicServiceConnector)
    singleOf(::CoverRepository)
    singleOf(::LyricRepository)
}

val ApiModule = module {
    single { GsonConverterFactory.create() }
    single { OkHttpClient.Builder().build() }
    single {
        Retrofit.Builder()
            .client(get())
            .addConverterFactory(get<GsonConverterFactory>())
            .baseUrl(LRCSHARE_BASEURL)
            .build()
            .create(LrcShareApi::class.java)
    }
}

val FilterModule = module {
    single<FilterGroup> {
        val settingSp: SettingsSp = get()
        val unknownArtistFilter = Filter(
            flow = settingSp.enableUnknownFilter.flow(true),
            getter = { it.metadata.artist },
            targetClass = LSong::class.java,
            ignoreRule = { flowValue, getterValue ->
                flowValue == true && getterValue == "<unknown>"
            }
        )
        val durationFilter = Filter(
            flow = settingSp.durationFilter.flow(true),
            getter = LSong::durationMs::get,
            targetClass = LSong::class.java,
            ignoreRule = { flowValue, getterValue ->
                getterValue <= (flowValue ?: 15)
            }
        )
        val excludePathFilter = Filter(
            flow = settingSp.excludePath.flow(true),
            getter = { it },
            targetClass = LSong::class.java,
            ignoreRule = { flowValue, getterValue ->
                if (flowValue.isNullOrEmpty()) return@Filter false
                // 排除目录功能只涉及 FileSystemScanner 和 MediaStoreScanner的
                if (getterValue.sourceType != SourceType.Local && getterValue.sourceType != SourceType.MediaStore)
                    return@Filter false

                val path = getterValue.fileInfo.directoryPath
                flowValue.any { path.startsWith(URLDecoder.decode(it, "UTF-8")) }
            }
        )

        FilterGroup.Builder()
            .add(unknownArtistFilter, durationFilter, excludePathFilter)
            .build()
    }
}