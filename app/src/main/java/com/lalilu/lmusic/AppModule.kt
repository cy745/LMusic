package com.lalilu.lmusic

import StatusBarLyric.API.StatusBarLyric
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.room.Room
import com.lalilu.R
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.database.LDatabase
import com.lalilu.lmedia.repository.FavoriteRepository
import com.lalilu.lmedia.repository.HistoryRepository
import com.lalilu.lmedia.repository.PlaylistRepository
import com.lalilu.lmedia.repository.impl.HistoryRepositoryImpl
import com.lalilu.lmedia.repository.impl.PlaylistRepositoryImpl
import com.lalilu.lmusic.datastore.LastPlayedSp
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.datastore.TempSp
import com.lalilu.lmusic.repository.CoverRepository
import com.lalilu.lmusic.repository.LMediaRepository
import com.lalilu.lmusic.repository.LyricRepository
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.service.notification.LMusicNotifier
import com.lalilu.lmusic.service.playback.helper.LMusicAudioFocusHelper
import com.lalilu.lmusic.service.playback.helper.LMusicNoisyReceiver
import com.lalilu.lmusic.service.playback.impl.LocalPlayer
import com.lalilu.lmusic.service.runtime.LMusicRuntime
import com.lalilu.lmusic.utils.extension.toBitmap
import com.lalilu.lmusic.utils.sources.EmbeddedLyricSource
import com.lalilu.lmusic.utils.sources.LocalLyricSource
import com.lalilu.lmusic.utils.sources.LyricSourceFactory
import com.lalilu.lmusic.viewmodel.AlbumsViewModel
import com.lalilu.lmusic.viewmodel.ArtistsViewModel
import com.lalilu.lmusic.viewmodel.DictionariesViewModel
import com.lalilu.lmusic.viewmodel.HistoryViewModel
import com.lalilu.lmusic.viewmodel.LMediaViewModel
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.PlaylistDetailViewModel
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel
import com.lalilu.lmusic.viewmodel.SearchViewModel
import com.lalilu.lmusic.viewmodel.SongDetailViewModel
import com.lalilu.lmusic.viewmodel.SongsViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.converter.gson.GsonConverterFactory

val AppModule = module {
    single { SettingsSp(androidApplication()) }
    single { LastPlayedSp(androidApplication()) }
    single { TempSp(androidApplication()) }
    single {
        StatusBarLyric(
            androidContext(),
            ContextCompat.getDrawable(androidContext(), R.mipmap.ic_launcher)?.toBitmap()
                ?.toDrawable(androidContext().resources),
            "com.lalilu.lmusic",
            false
        )
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
            getSongOrNull = LMedia::getSongOrNull
        )
    }
}

val ViewModelModule = module {
    single { PlayingViewModel(get(), get(), get(), get()) }
    single { LibraryViewModel(get(), get()) }
    single { LMediaViewModel(get()) }
    single { PlaylistDetailViewModel(get(), get()) }
    single { PlaylistsViewModel(get(), get()) }
    single { SearchViewModel(get(), get()) }
    single { AlbumsViewModel(get(), get()) }
    single { ArtistsViewModel(get(), get()) }
    single { DictionariesViewModel(get(), get()) }
    single { HistoryViewModel(get(), get()) }
    single { SongsViewModel(get(), get(), get()) }
    single { SongDetailViewModel(get()) }
}

val PlayerModule = module {
    single { LMusicAudioFocusHelper(androidApplication(), get()) }
    single { LMusicNoisyReceiver(androidApplication()) }
    single { LocalPlayer(androidApplication()) }
}

val RuntimeModule = module {
    single { LMusicNotifier(get(), get(), get(), get(), androidApplication()) }
    single { LMusicBrowser(get(), get(), get(), get()) }
    single { LMusicRuntime(get(), get()) }
    single { CoverRepository(get()) }
    single { LyricRepository(get(), get()) }
    single { LMediaRepository() }

    single { LyricSourceFactory(get(), get()) }
    single { EmbeddedLyricSource() }
    single { LocalLyricSource() }
}

val ApiModule = module {
    single { GsonConverterFactory.create() }
    single { OkHttpClient.Builder().build() }
}