package com.lalilu.common.kv

import com.blankj.utilcode.util.EncryptUtils
import io.fastkv.FastKV

abstract class KVMapItem<T>(
    private val key: String,
    private val fastKV: FastKV
) : KVItem<Map<String, T>>() {
    private val identityKey by lazy {
        if (key.length <= 20) return@lazy key

        key.take(20) + EncryptUtils.encryptMD5ToString(key).take(8)
    }

    companion object {
        const val mapKeyTemplate = "#MAP_%s"
        const val itemKeyTemplate = "#ITEM_%s_%s"
    }

    protected abstract fun set(key: String, value: T?)
    protected abstract fun get(key: String): T?

    override fun get(): Map<String, T>? {
        val mapKey = mapKeyTemplate.format(identityKey)

        return fastKV.getStringSet(mapKey)
            .mapNotNull { str ->
                str?.let { itemKeyTemplate.format(identityKey, it) }
                    ?.let { get(str) }
                    ?.let { str to it }
            }
            .toMap()
    }

    override fun set(value: Map<String, T>?) {
        super.set(value)

        val mapKey = mapKeyTemplate.format(identityKey)
        if (value == null) {
            fastKV.remove(mapKey)
            return
        }

        if (value.isEmpty()) {
            fastKV.putStringSet(mapKey, emptySet())
            return
        }

        for (str in value.entries) {
            val itemKey = itemKeyTemplate.format(identityKey, itemKeyTemplate)
            set(itemKey, str.value)
        }
        fastKV.putStringSet(mapKey, value.keys)
    }
}