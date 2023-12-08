package com.lalilu.lartist

import com.lalilu.lartist.screen.ArtistsScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val ArtistModule = module {
    factoryOf(::ArtistsScreenModel)
}