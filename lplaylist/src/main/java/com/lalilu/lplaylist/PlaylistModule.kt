package com.lalilu.lplaylist

import com.lalilu.lplaylist.repository.PlaylistRepository
import com.lalilu.lplaylist.repository.PlaylistRepositoryImpl
import com.lalilu.lplaylist.screen.create.PlaylistCreateOrEditScreenModel
import com.lalilu.lplaylist.screen.detail.PlaylistDetailScreenModel
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

@Module
@ComponentScan("com.lalilu.lplaylist")
object PlaylistModule2

val PlaylistModule = module {
    singleOf(::PlaylistRepositoryImpl)
    factoryOf(::PlaylistDetailScreenModel)
    factoryOf(::PlaylistCreateOrEditScreenModel)

    single<PlaylistRepository> { get<PlaylistRepositoryImpl>() }
}