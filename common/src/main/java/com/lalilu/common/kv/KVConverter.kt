package com.lalilu.common.kv

import kotlin.reflect.KClass

interface KVConverter {
    fun convert(value: Any?): String
    fun restore(content: String): Any?
    fun accept(baseType: KClass<*>?, clazz: KClass<*>, default: Any?): Boolean
}