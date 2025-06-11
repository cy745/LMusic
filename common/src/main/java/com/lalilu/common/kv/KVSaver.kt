package com.lalilu.common.kv

import kotlin.reflect.KClass

interface KVSaver {
    fun <T> readData(
        key: String,
        defaultValue: T?,
        baseType: KClass<*>? = null,
        clazz: KClass<*>,
    ): T

    fun <T> saveData(
        key: String,
        value: T?,
        baseType: KClass<*>? = null,
        clazz: KClass<*>,
    )
}