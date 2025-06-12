package com.lalilu.common.kv.impl

import com.lalilu.common.kv.KVContext
import com.lalilu.common.kv.KVItem
import com.lalilu.common.kv.KVSaver
import kotlin.reflect.KClass

class KVItemImpl<T>(
    val key: String,
    val clazz: KClass<*>,
    val baseType: KClass<*>? = null,
    val defaultValue: T? = null,
) : KVItem<T>() {
    private val saver: KVSaver by lazy { requireNotNull(KVContext.kvSaver) { "KvSaver is not set" } }
    private val converter by lazy { KVContext.findConverter(baseType, clazz, defaultValue) }
    private val convertedDefaultValue: String? by lazy { converter?.convert(defaultValue) }

    override fun getData(): T {
        if (converter == null) {
            return saver.readData(key, defaultValue, baseType, clazz)
        }

        val data = saver.readData(key, convertedDefaultValue, baseType, clazz)
        if (data.isBlank()) return defaultValue
            ?: throw IllegalStateException("default value not provided. key: $key")

        return runCatching { converter!!.restore(data) as? T }
            .getOrNull()
            ?: defaultValue
            ?: throw IllegalStateException("convert failed, and default value not provided. key: $key, value: $data")
    }

    override fun setData(value: T) {
        if (converter == null) {
            saver.saveData(key, value, baseType, clazz)
            super.setData(value)
            return
        }

        val data = converter!!.convert(value)
        saver.saveData(key, data, baseType, clazz)
        super.setData(value)
    }

    override fun remove() {
        saver.saveData(key, null, baseType, clazz)
        update()
    }
}

