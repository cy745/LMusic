package com.lalilu.lplaylist

import com.lalilu.lplaylist.repository.PlaylistKV
import com.lalilu.lplaylist.repository.PlaylistRepository
import com.lalilu.lplaylist.repository.PlaylistRepositoryImpl
import com.lalilu.lplaylist.screen.PlaylistCreateOrEditScreenModel
import com.lalilu.lplaylist.screen.PlaylistDetailScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val PlaylistModule = module {
    singleOf(::PlaylistKV)

    singleOf(::PlaylistRepositoryImpl)
    factoryOf(::PlaylistDetailScreenModel)
    factoryOf(::PlaylistCreateOrEditScreenModel)

    single<PlaylistRepository> { get<PlaylistRepositoryImpl>() }
}