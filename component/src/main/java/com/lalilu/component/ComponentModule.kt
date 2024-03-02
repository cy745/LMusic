package com.lalilu.component

import com.lalilu.component.viewmodel.SongsSp
import com.lalilu.component.viewmodel.SongsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val ComponentModule = module {
    single<SongsSp> { SongsSp(androidApplication()) }
    viewModelOf(::SongsViewModel)
}