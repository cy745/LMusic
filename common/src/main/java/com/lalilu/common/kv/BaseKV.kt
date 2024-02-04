package com.lalilu.common.kv

import io.fastkv.FastKV
import io.fastkv.interfaces.FastEncoder

@Suppress("UNCHECKED_CAST")
abstract class BaseKV {
    val kvMap = LinkedHashMap<String, KVItem<out Any>>()
    abstract val fastKV: FastKV

    inline fun <reified T : Any> obtain(key: String): KVItem<T> = kvMap.getOrPut(key) {
        when {
            T::class.java.isAssignableFrom(Int::class.java) -> IntKVItem(key, fastKV)
            T::class.java.isAssignableFrom(Long::class.java) -> LongKVItem(key, fastKV)
            T::class.java.isAssignableFrom(Float::class.java) -> FloatKVItem(key, fastKV)
            T::class.java.isAssignableFrom(Double::class.java) -> DoubleKVItem(key, fastKV)
            T::class.java.isAssignableFrom(Boolean::class.java) -> BooleanKVItem(key, fastKV)
            T::class.java.isAssignableFrom(String::class.java) -> StringKVItem(key, fastKV)
            T::class.java.isAssignableFrom(ByteArray::class.java) -> ByteArrayKVItem(key, fastKV)
            else -> throw IllegalArgumentException("Unsupported type")
        }
    } as KVItem<T>

    inline fun <reified T : Any> obtainList(key: String): KVItem<List<T>> = kvMap.getOrPut(key) {
        when {
            T::class.java.isAssignableFrom(Int::class.java) -> IntListKVItem(key, fastKV)
            T::class.java.isAssignableFrom(Long::class.java) -> LongListKVItem(key, fastKV)
            T::class.java.isAssignableFrom(Float::class.java) -> FloatListKVItem(key, fastKV)
            T::class.java.isAssignableFrom(Double::class.java) -> DoubleListKVItem(key, fastKV)
            T::class.java.isAssignableFrom(Boolean::class.java) -> BooleanListKVItem(key, fastKV)
            T::class.java.isAssignableFrom(String::class.java) -> StringListKVItem(key, fastKV)
            else -> throw IllegalArgumentException("Unsupported type")
        }
    } as KVItem<List<T>>

    inline fun <reified T : Any> obtain(key: String, encoder: FastEncoder<T>): KVItem<T> =
        kvMap.getOrPut(key) { ObjectKVItem(key, fastKV, encoder) } as KVItem<T>

    inline fun <reified T : Any> obtainList(key: String, encoder: FastEncoder<T>): KVListItem<T> =
        kvMap.getOrPut(key) { ObjectListKVItem(key, fastKV, encoder) } as KVListItem<T>
}
