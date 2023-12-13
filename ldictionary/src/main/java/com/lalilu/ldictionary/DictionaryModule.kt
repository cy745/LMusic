package com.lalilu.ldictionary

import com.lalilu.ldictionary.screen.DictionaryScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val DictionaryModule = module {
    factoryOf(::DictionaryScreenModel)
}