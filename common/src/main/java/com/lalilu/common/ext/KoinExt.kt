package com.lalilu.common.ext

import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent

/**
 * Koin 快速获取对象实例
 */
inline fun <reified T> requestFor(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T? = KoinJavaComponent.getOrNull(T::class.java, qualifier, parameters)

/**
 * Koin 快速获取对象实例
 */
inline fun <reified T> requestFor(
    vararg key: String
): Set<T> = key.mapNotNull { requestFor<T>(named(it)) }
    .toSet()
