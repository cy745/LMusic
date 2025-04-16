package com.lalilu.common.kv

import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import java.io.Serializable


class BoolKVImpl(
    val key: String,
) : KVItem<Boolean>() {
    override fun get(): Boolean? {
        if (!SPUtils.getInstance().contains(key)) return null
        return SPUtils.getInstance().getBoolean(key)
    }

    override fun set(value: Boolean?) {
        super.set(value)
        if (value == null) {
            SPUtils.getInstance().remove(key)
        } else {
            SPUtils.getInstance().put(key, value)
        }
    }
}

class IntKVImpl(
    val key: String,
) : KVItem<Int>() {
    override fun get(): Int? {
        if (!SPUtils.getInstance().contains(key)) return null
        return SPUtils.getInstance().getInt(key)
    }

    override fun set(value: Int?) {
        super.set(value)
        if (value == null) {
            SPUtils.getInstance().remove(key)
        } else {
            SPUtils.getInstance().put(key, value)
        }
    }
}

class LongKVImpl(
    val key: String,
) : KVItem<Long>() {
    override fun get(): Long? {
        if (!SPUtils.getInstance().contains(key)) return null
        return SPUtils.getInstance().getLong(key)
    }

    override fun set(value: Long?) {
        super.set(value)
        if (value == null) {
            SPUtils.getInstance().remove(key)
        } else {
            SPUtils.getInstance().put(key, value)
        }
    }
}

class FloatKVImpl(
    val key: String,
) : KVItem<Float>() {
    override fun get(): Float? {
        if (!SPUtils.getInstance().contains(key)) return null
        return SPUtils.getInstance().getFloat(key)
    }

    override fun set(value: Float?) {
        super.set(value)
        if (value == null) {
            SPUtils.getInstance().remove(key)
        } else {
            SPUtils.getInstance().put(key, value)
        }
    }
}

class StringKVImpl(
    val key: String,
) : KVItem<String>() {
    override fun get(): String? {
        if (!SPUtils.getInstance().contains(key)) return null
        return SPUtils.getInstance().getString(key)
    }

    override fun set(value: String?) {
        super.set(value)
        if (value == null) {
            SPUtils.getInstance().remove(key)
        } else {
            SPUtils.getInstance().put(key, value)
        }
    }
}

class DoubleKVImpl(
    val key: String,
) : KVItem<Double>() {
    override fun get(): Double? {
        if (!SPUtils.getInstance().contains(key)) return null
        val bitValue = SPUtils.getInstance().getLong(key)
        return Double.fromBits(bitValue)
    }

    override fun set(value: Double?) {
        super.set(value)
        if (value == null) {
            SPUtils.getInstance().remove(key)
        } else {
            val bitValue = value.toRawBits()
            SPUtils.getInstance().put(key, bitValue)
        }
    }
}

class ObjectKVImpl<T : Serializable>(
    private val key: String,
    private val clazz: Class<T>
) : KVItem<T>() {
    override fun get(): T? {
        if (!SPUtils.getInstance().contains(key)) return null
        val json = SPUtils.getInstance().getString(key)
        return GsonUtils.fromJson(json, clazz)
    }

    override fun set(value: T?) {
        super.set(value)
        if (value == null) {
            SPUtils.getInstance().remove(key)
        } else {
            val json = GsonUtils.toJson(value)
            SPUtils.getInstance().put(key, json)
        }
    }
}

