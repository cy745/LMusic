package com.lalilu.common.kv

import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.LogUtils
import io.fastkv.FastKV

abstract class KVListItem<T>(
    private val key: String,
    private val fastKV: FastKV
) : KVItem<List<T>>() {
    private val identityKey by lazy {
        if (key.length <= 20) return@lazy key

        key.take(20) + EncryptUtils.encryptMD5ToString(key).take(8)
    }

    companion object {
        const val countKeyTemplate = "#COUNT_%s"
        const val valueKeyTemplate = "#INDEX_%s_%d"
    }

    protected abstract fun set(key: String, value: T?)
    protected abstract fun get(key: String): T?

    override fun set(value: List<T>?) {
        super.set(value)
        val countKey = countKeyTemplate.format(identityKey)

        // 若列表为null，则删除计数键
        if (value == null) {
            fastKV.remove(countKey)
            return
        }

        // 若列表元素为空，则设计数键为0
        if (value.isEmpty()) {
            fastKV.putInt(countKey, 0)
            return
        }

        // 先进行数据存入
        for (index in value.indices) {
            val valueKey = valueKeyTemplate.format(identityKey, index)
            set(valueKey, value[index])
        }

        // 后更新计数键值，避免数据更新失败时导致计数键丢失，进而导致后期读取时异常
        fastKV.putInt(countKey, value.size)
    }

    override fun get(): List<T>? {
        val countKey = countKeyTemplate.format(identityKey)

        if (!fastKV.contains(countKey)) {
            LogUtils.i("[$key] is undefined, return null [countKey: $countKey]")
            return null
        }

        val count = fastKV.getInt(countKey)
        if (count == 0) {
            LogUtils.i("[$key] is empty, return emptyList [countKey: $countKey]")
            return emptyList()
        }

        return (0..count).mapNotNull {
            val valueKey = valueKeyTemplate.format(identityKey, it)
            get(valueKey)
        }
    }
}