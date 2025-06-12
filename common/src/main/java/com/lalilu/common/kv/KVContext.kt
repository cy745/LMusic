package com.lalilu.common.kv

import com.lalilu.common.kv.impl.KVItemImpl
import com.lalilu.common.kv.impl.StringListKVConverter
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
abstract class KVContext(
    val _prefix: String = ""
) {
    inline fun <reified T> obtain(
        key: String,
        defaultValue: T? = null,
        prefix: String = _prefix,
    ): KVItem<T> = obtainStatic(key, defaultValue, prefix)

    inline fun <reified T> obtainList(
        key: String,
        defaultValue: List<T> = emptyList<T>(),
        prefix: String = _prefix,
    ): KVItem<List<T>> = obtainListStatic(key, defaultValue, prefix)

    companion object {
        val converters = mutableListOf<KVConverter>(StringListKVConverter())
        val kvMap = LinkedHashMap<String, KVItem<*>>()
        var kvSaver: KVSaver? = KVSpSaver
            private set

        fun registerSaver(kvSaver: KVSaver) {
            this.kvSaver = kvSaver
        }

        fun registerConverter(converter: KVConverter) {
            converters += converter
        }

        fun findConverter(baseType: KClass<*>?, clazz: KClass<*>, default: Any?): KVConverter? {
            val converter = converters.firstOrNull { it.accept(baseType, clazz, default) }
                    as? KVConverter

            if (baseType != null) {
                requireNotNull(converter) {
                    "No KVConverter found for ${clazz.simpleName} with baseType $baseType"
                }
            }

            return converter
        }

        inline fun <reified T> obtainStatic(
            key: String,
            defaultValue: T? = null,
            prefix: String = "",
        ): KVItem<T> {
            val actualKey = if (prefix.isNotBlank()) "${prefix}_$key" else key
            return kvMap.getOrPut(actualKey) {
                KVItemImpl<T>(
                    key = actualKey,
                    clazz = T::class,
                    defaultValue = defaultValue,
                )
            } as KVItem<T>
        }

        inline fun <reified T> obtainListStatic(
            key: String,
            defaultValue: List<T> = emptyList<T>(),
            prefix: String = "",
        ): KVItem<List<T>> {
            val actualKey = if (prefix.isNotBlank()) "${prefix}_$key" else key

            return kvMap.getOrPut(actualKey) {
                KVItemImpl<List<T>>(
                    key = actualKey,
                    clazz = T::class,
                    baseType = List::class,
                    defaultValue = defaultValue,
                )
            } as KVItem<List<T>>
        }
    }
}
