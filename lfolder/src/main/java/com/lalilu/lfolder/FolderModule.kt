package com.lalilu.lfolder

import com.lalilu.lfolder.screen.DictionaryScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val FolderModule = module {
    factoryOf(::DictionaryScreenModel)
}