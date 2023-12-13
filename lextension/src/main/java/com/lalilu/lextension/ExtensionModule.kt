package com.lalilu.lextension

import com.lalilu.lextension.repository.ExtensionSp
import com.lalilu.lextension.component.ExtensionsScreenModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val ExtensionModule = module {
    single<ExtensionSp> { ExtensionSp(androidApplication()) }

    factoryOf(::ExtensionsScreenModel)
}