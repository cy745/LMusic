package com.lalilu.common.kv

import com.lalilu.common.kv.impl.KVItemImpl
import com.lalilu.common.kv.impl.StringListKVConverter
import java.io.Serializable
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
abstract class KVContext(
    val _prefix: String = ""
) {
    inline fun <reified T : Serializable> obtain(
        key: String,
        prefix: String = _prefix,
        defaultValue: T? = null
    ): KVItem<T> = obtainStatic(key, prefix, defaultValue)

    inline fun <reified T : Serializable> obtainList(
        key: String,
        prefix: String = _prefix,
        defaultValue: List<T>? = null,
    ): KVItem<List<T>> = obtainListStatic(key, prefix, defaultValue)

    companion object {
        val converters = mutableListOf<KVConverter>(StringListKVConverter())
        val kvMap = LinkedHashMap<String, KVItem<out Any>>()
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

            requireNotNull(converter) {
                "No KVConverter found for ${clazz.simpleName} with baseType $baseType"
            }

            return converter
        }

        inline fun <reified T : Serializable> obtainStatic(
            key: String,
            prefix: String = "",
            defaultValue: T? = null
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

        inline fun <reified T : Serializable> obtainListStatic(
            key: String,
            prefix: String = "",
            defaultValue: List<T>? = null,
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
