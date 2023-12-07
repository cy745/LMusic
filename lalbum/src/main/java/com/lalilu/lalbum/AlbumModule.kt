package com.lalilu.lalbum

import com.lalilu.lalbum.screen.AlbumDetailScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val AlbumModule = module {
    factoryOf(::AlbumDetailScreenModel)
}