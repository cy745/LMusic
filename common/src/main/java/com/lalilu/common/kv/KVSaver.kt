package com.lalilu.common.kv

import kotlin.reflect.KClass

interface KVSaver {
    fun <T> readData(
        key: String,
        defaultValue: T?,
        clazz: KClass<*>,
    ): T

    fun <T> saveData(
        key: String,
        value: T?,
        clazz: KClass<*>,
    )
}