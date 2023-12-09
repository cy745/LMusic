package com.lalilu.lextension

import com.lalilu.lextension.screen.ExtensionsScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val ExtensionModule = module {
    factoryOf(::ExtensionsScreenModel)
}