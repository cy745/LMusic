package com.lalilu.lmusic

import StatusBarLyric.API.StatusBarLyric
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.room.Room
import coil.ImageLoader
import com.blankj.utilcode.util.FileUtils
import com.lalilu.R
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.database.LDatabase
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.indexer.Filter
import com.lalilu.lmedia.indexer.FilterGroup
import com.lalilu.lmedia.repository.FavoriteRepository
import com.lalilu.lmedia.repository.HistoryRepository
import com.lalilu.lmedia.repository.PlaylistRepository
import com.lalilu.lmedia.repository.impl.HistoryRepositoryImpl
import com.lalilu.lmedia.repository.impl.PlaylistRepositoryImpl
import com.lalilu.lmusic.Config.LRCSHARE_BASEURL
import com.lalilu.lmusic.api.lrcshare.LrcShareApi
import com.lalilu.lmusic.compose.new_screen.ExtensionsViewModel
import com.lalilu.lmusic.datastore.LastPlayedSp
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.datastore.TempSp
import com.lalilu.lmusic.repository.CoverRepository
import com.lalilu.lmusic.repository.LMediaRepository
import com.lalilu.lmusic.repository.LyricRepository
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.service.LMusicNotifier
import com.lalilu.lmusic.service.LMusicRuntime
import com.lalilu.lmusic.utils.EQHelper
import com.lalilu.lmusic.utils.coil.CrossfadeTransitionFactory
import com.lalilu.lmusic.utils.coil.fetcher.AlbumCoverFetcher
import com.lalilu.lmusic.utils.coil.fetcher.SongCoverFetcher
import com.lalilu.lmusic.utils.coil.keyer.SongCoverKeyer
import com.lalilu.lmusic.utils.extension.toBitmap
import com.lalilu.lmusic.viewmodel.AlbumsViewModel
import com.lalilu.lmusic.viewmodel.ArtistsViewModel
import com.lalilu.lmusic.viewmodel.DictionariesViewModel
import com.lalilu.lmusic.viewmodel.HistoryViewModel
import com.lalilu.lmusic.viewmodel.LMediaViewModel
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.PlaylistDetailViewModel
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel
import com.lalilu.lmusic.viewmodel.SearchLyricViewModel
import com.lalilu.lmusic.viewmodel.SearchViewModel
import com.lalilu.lmusic.viewmodel.SongDetailViewModel
import com.lalilu.lmusic.viewmodel.SongsViewModel
import com.lalilu.lplayer.notification.Notifier
import com.lalilu.lplayer.runtime.Runtime
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val AppModule = module {
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
                add(AlbumCoverFetcher.AlbumFactory())
                add(SongCoverFetcher.SongFactory(get<OkHttpClient>()))
                add(SongCoverKeyer())
            }
            .transitionFactory(CrossfadeTransitionFactory())
            .build()
    }
}

val DatabaseModule = module {
    single {
        Room.databaseBuilder(androidApplication(), LDatabase::class.java, "lmedia_database.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    single<HistoryRepository> { HistoryRepositoryImpl(get<LDatabase>().historyDao()) }
    single<PlaylistRepository> { get<PlaylistRepositoryImpl>() }
    single<FavoriteRepository> { get<PlaylistRepositoryImpl>() }
    single {
        PlaylistRepositoryImpl(
            playlistDao = get<LDatabase>().playlistDao(),
            songInPlaylistDao = get<LDatabase>().songInPlaylistDao(),
            getSongOrNull = { LMedia.get<LSong>(it) }
        )
    }
}

val ViewModelModule = module {
    single { PlayingViewModel(get(), get(), get(), get()) }
    single { LibraryViewModel(get()) }
    single { LMediaViewModel(get()) }
    single { PlaylistDetailViewModel(get(), get()) }
    single { PlaylistsViewModel(get(), get()) }
    single { SearchViewModel(get()) }
    single { AlbumsViewModel(get(), get()) }
    single { ArtistsViewModel(get(), get()) }
    single { DictionariesViewModel(get()) }
    single { HistoryViewModel(get()) }
    single { SongsViewModel(get(), get(), get()) }
    single { SongDetailViewModel(get()) }
    single { SearchLyricViewModel(androidApplication(), get(), get()) }
    single { ExtensionsViewModel(androidApplication()) }
}

val RuntimeModule = module {
    single<Notifier> { LMusicNotifier(androidApplication(), get(), get(), get(), get()) }
    single<Runtime<LSong>> { get<LMusicRuntime>() }
    single { LMusicRuntime(get()) }

    single { LMusicBrowser(get(), get(), get()) }
    single { CoverRepository(get()) }
    single { LyricRepository(get(), get()) }
    single { LMediaRepository() }
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
            getter = LSong::_artist::get,
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
        val pathFilter = Filter(
            flow = settingSp.blockedPaths.flow(true),
            getter = LSong::pathStr::get,
            targetClass = LSong::class.java,
            ignoreRule = { flowValue, getterValue ->
                if (flowValue.isNullOrEmpty()) return@Filter false

                val path = FileUtils.getDirName(getterValue)
                    ?.takeIf(String::isNotEmpty)
                    ?: "Unknown dir"
                path in flowValue
            }
        )
        FilterGroup.Builder()
            .add(unknownArtistFilter, durationFilter, pathFilter)
            .build()
    }
}