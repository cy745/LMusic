package com.lalilu.common.ext

import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.java.KoinJavaComponent

inline fun <reified T> requestFor(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T? = KoinJavaComponent.getOrNull(T::class.java, qualifier, parameters)