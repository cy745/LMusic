package com.lalilu.lplaylist

import com.lalilu.lplaylist.repository.PlaylistRepository
import com.lalilu.lplaylist.repository.PlaylistRepositoryImpl
import com.lalilu.lplaylist.repository.PlaylistSp
import com.lalilu.lplaylist.viewmodel.PlaylistViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val PlaylistModule = module {
    single { PlaylistSp(androidApplication()) }
    single<PlaylistRepository> { PlaylistRepositoryImpl(get()) }
    viewModelOf(::PlaylistViewModel)
}