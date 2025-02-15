package com.lalilu.lmusic

import StatusBarLyric.API.StatusBarLyric
import android.app.Application
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModelStoreOwner
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.transitionFactory
import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.core.DataSaverPreferences
import com.lalilu.R
import com.lalilu.lmusic.Config.LRCSHARE_BASEURL
import com.lalilu.lmusic.api.lrcshare.LrcShareApi
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.datastore.TempSp
import com.lalilu.lmusic.utils.EQHelper
import com.lalilu.lmusic.utils.coil.CrossfadeTransitionFactory
import com.lalilu.lmusic.utils.coil.fetcher.LAlbumFetcher
import com.lalilu.lmusic.utils.coil.fetcher.LSongFetcher
import com.lalilu.lmusic.utils.coil.fetcher.MediaItemFetcher
import com.lalilu.lmusic.utils.coil.keyer.LAlbumCoverKeyer
import com.lalilu.lmusic.utils.coil.keyer.LSongCoverKeyer
import com.lalilu.lmusic.utils.coil.keyer.MediaItemKeyer
import com.lalilu.lmusic.utils.extension.toBitmap
import com.lalilu.lmusic.viewmodel.SearchLyricViewModel
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@ComponentScan("com.lalilu.lmusic")
object MainModule

@Single
fun provideDataSaverInterface(
    application: Application
): DataSaverInterface {
    val sp = application.getSharedPreferences("settings", Application.MODE_PRIVATE)
    return DataSaverPreferences(sp)
}

@Single
fun provideJson(): Json {
    return Json {
        ignoreUnknownKeys = true
    }
}

@Single(createdAtStart = true)
fun provideImageLoaderFactory(
    context: Application,
    client: OkHttpClient,
): SingletonImageLoader.Factory {
    return SingletonImageLoader.Factory {
        ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(client))
                add(LSongCoverKeyer())
                add(LAlbumCoverKeyer())
                add(MediaItemKeyer())
                add(LSongFetcher.SongFactory())
                add(LAlbumFetcher.AlbumFactory())
                add(MediaItemFetcher.MediaItemFetcherFactory())
            }
            .transitionFactory(CrossfadeTransitionFactory())
//            .logger(DebugLogger())
            .build()
    }
}

val AppModule = module {
    single<ViewModelStoreOwner> { androidApplication() as ViewModelStoreOwner }
    single { SettingsSp(androidApplication()) }
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
}

val ViewModelModule = module {
    viewModelOf(::SearchLyricViewModel)
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