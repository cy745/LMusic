package com.lalilu.lplaylist

import com.lalilu.lplaylist.repository.PlaylistRepository
import com.lalilu.lplaylist.repository.PlaylistRepositoryImpl
import com.lalilu.lplaylist.repository.PlaylistSp
import com.lalilu.lplaylist.screen.PlaylistDetailScreenModel
import com.lalilu.lplaylist.screen.PlaylistCreateOrEditScreenModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val PlaylistModule = module {
    single { PlaylistSp(androidApplication()) }
    singleOf(::PlaylistRepositoryImpl)
    factoryOf(::PlaylistDetailScreenModel)
    factoryOf(::PlaylistCreateOrEditScreenModel)

    single<PlaylistRepository> { get<PlaylistRepositoryImpl>() }
}