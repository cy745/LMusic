package com.lalilu.common.kv

import java.io.Serializable

@Suppress("UNCHECKED_CAST")
abstract class BaseKV(val prefix: String = "") {
    val kvMap = LinkedHashMap<String, KVItem<out Any>>()

    inline fun <reified T : Serializable> obtain(key: String): KVItem<T> {
        val actualKey = if (prefix.isNotBlank()) "${prefix}_$key" else key
        return kvMap.getOrPut(actualKey) {
            when {
                T::class == Boolean::class -> BoolKVImpl(actualKey)
                T::class == Int::class -> IntKVImpl(actualKey)
                T::class == Long::class -> LongKVImpl(actualKey)
                T::class == Float::class -> FloatKVImpl(actualKey)
                T::class == Double::class -> DoubleKVImpl(actualKey)
                T::class == String::class -> StringKVImpl(actualKey)
                else -> ObjectKVImpl(actualKey, T::class.java)
            }
        } as KVItem<T>
    }

    inline fun <reified T : Serializable> obtainList(key: String): KVListItem<T> {
        val actualKey = if (prefix.isNotBlank()) "${prefix}_$key" else key

        return kvMap.getOrPut(actualKey) {
            when {
                T::class == Boolean::class -> BoolListKVImpl(actualKey)
                T::class == Int::class -> IntListKVImpl(actualKey)
                T::class == Long::class -> LongListKVImpl(actualKey)
                T::class == Float::class -> FloatListKVImpl(actualKey)
                T::class == Double::class -> DoubleListKVImpl(actualKey)
                T::class == String::class -> StringListKVImpl(actualKey)
                else -> ObjectListKVImpl(actualKey, T::class.java)
            }
        } as KVListItem<T>
    }
}
