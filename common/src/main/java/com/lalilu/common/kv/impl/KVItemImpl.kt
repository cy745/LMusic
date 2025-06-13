package com.lalilu.common.kv.impl

import com.lalilu.common.kv.KVContext
import com.lalilu.common.kv.KVConverter
import com.lalilu.common.kv.KVItem
import com.lalilu.common.kv.KVSaver
import kotlin.reflect.KClass

class KVItemImpl<T : Any>(
    val key: String,
    val clazz: KClass<T>,
    val defaultValue: T? = null,
    val converter: KVConverter? = null,
) : KVItem<T>() {
    private val saver: KVSaver by lazy { requireNotNull(KVContext.kvSaver) { "KvSaver is not set" } }
    private val convertedDefaultValue: String? by lazy {
        defaultValue?.runCatching { converter?.convert(this) }
            ?.getOrNull()
    }

    @Suppress("UNCHECKED_CAST")
    override fun getData(): T {
        if (converter == null || convertedDefaultValue == null) {
            return saver.readData(key, defaultValue, clazz)
        }

        val data = saver.readData(key, convertedDefaultValue, clazz)
        if (data.isBlank()) return defaultValue
            ?: throw IllegalStateException("default value not provided. key: $key")

        return runCatching { converter!!.restore(data) as? T }
            .getOrNull()
            ?: defaultValue
            ?: throw IllegalStateException("convert failed, and default value not provided. key: $key, value: $data")
    }

    override fun setData(value: T) {
        if (converter == null) {
            saver.saveData(key, value, clazz)
            super.setData(value)
            return
        }

        val data = converter.convert(value)
        saver.saveData(key, data, clazz)
        super.setData(value)
    }

    override fun remove() {
        saver.saveData(key, null, clazz)
        update()
    }
}

