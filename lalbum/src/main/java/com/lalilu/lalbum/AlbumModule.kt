package com.lalilu.lalbum

import com.lalilu.lalbum.screen.AlbumDetailScreenModel
import com.lalilu.lalbum.screen.AlbumsScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val AlbumModule = module {
    factoryOf(::AlbumDetailScreenModel)
    factoryOf(::AlbumsScreenModel)
}