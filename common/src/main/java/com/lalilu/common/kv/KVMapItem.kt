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
    private val mapKey by lazy { mapKeyTemplate.format(identityKey) }

    companion object {
        const val mapKeyTemplate = "#MAP_%s"
        const val itemKeyTemplate = "#ITEM_%s_%s"
    }

    protected abstract fun set(key: String, value: T?)
    protected abstract fun get(key: String): T?

    override fun get(): Map<String, T>? {
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

        if (value == null) {
            fastKV.remove(mapKey)
            return
        }

        if (value.isEmpty()) {
            fastKV.putStringSet(mapKey, emptySet())
            return
        }

        for (str in value.entries) {
            val itemKey = itemKeyTemplate.format(identityKey, str.key)
            set(itemKey, str.value)
        }
        fastKV.putStringSet(mapKey, value.keys)
    }
}