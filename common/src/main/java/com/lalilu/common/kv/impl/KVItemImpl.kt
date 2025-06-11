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

    override fun get(): T? {
        if (converter == null) {
            return saver.readData(key, defaultValue, baseType, clazz)
        }

        val data = saver.readData(key, convertedDefaultValue, baseType, clazz)
        return converter!!.restore(data) as T
    }

    override fun set(value: T?) {
        if (converter == null || value == null) {
            saver.saveData(key, value, baseType, clazz)
            super.set(value)
            return
        }

        val data = converter!!.convert(value)
        saver.saveData(key, data, baseType, clazz)
        super.set(value)
    }
}

