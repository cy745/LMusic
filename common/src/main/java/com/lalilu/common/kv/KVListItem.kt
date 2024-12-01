package com.lalilu.common.kv

import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import com.google.common.reflect.TypeToken
import java.io.Serializable

abstract class KVListItem<T : Serializable> : KVItem<List<T>>()

@Suppress("UnstableApiUsage")
class StringListKVImpl(
    private val key: String
) : KVListItem<String>() {
    companion object {
        val typeToken by lazy { object : TypeToken<List<String>>() {} }
    }

    override fun get(): List<String>? {
        val json = SPUtils.getInstance().getString(key)
        return GsonUtils.fromJson<List<String>>(json, typeToken.type)
    }

    override fun set(value: List<String>?) {
        super.set(value)

        if (value == null) {
            SPUtils.getInstance().remove(key)
        } else {
            val json = GsonUtils.toJson(value)
            SPUtils.getInstance().put(key, json)
        }
    }
}

@Suppress("UnstableApiUsage")
class IntListKVImpl(
    private val key: String
) : KVListItem<Int>() {
    companion object {
        val typeToken by lazy { object : TypeToken<List<Int>>() {} }
    }

    override fun get(): List<Int>? {
        val json = SPUtils.getInstance().getString(key)
        return GsonUtils.fromJson<List<Int>>(json, typeToken.type)
    }

    override fun set(value: List<Int>?) {
        super.set(value)

        if (value == null) {
            SPUtils.getInstance().remove(key)
        } else {
            val json = GsonUtils.toJson(value)
            SPUtils.getInstance().put(key, json)
        }
    }
}

@Suppress("UnstableApiUsage")
class LongListKVImpl(
    private val key: String
) : KVListItem<Long>() {
    companion object {
        val typeToken by lazy { object : TypeToken<List<Long>>() {} }
    }

    override fun get(): List<Long>? {
        val json = SPUtils.getInstance().getString(key)
        return GsonUtils.fromJson<List<Long>>(json, typeToken.type)
    }

    override fun set(value: List<Long>?) {
        super.set(value)
        if (value == null) {
            SPUtils.getInstance().remove(key)
        } else {
            val json = GsonUtils.toJson(value)
            SPUtils.getInstance().put(key, json)
        }
    }
}

@Suppress("UnstableApiUsage")
class FloatListKVImpl(
    private val key: String
) : KVListItem<Float>() {
    companion object {
        val typeToken by lazy { object : TypeToken<List<Float>>() {} }
    }

    override fun get(): List<Float>? {
        val json = SPUtils.getInstance().getString(key)
        return GsonUtils.fromJson<List<Float>>(json, typeToken.type)
    }

    override fun set(value: List<Float>?) {
        super.set(value)
        if (value == null) {
            SPUtils.getInstance().remove(key)
        } else {
            val json = GsonUtils.toJson(value)
            SPUtils.getInstance().put(key, json)
        }
    }
}

@Suppress("UnstableApiUsage")
class DoubleListKVImpl(
    private val key: String
) : KVListItem<Double>() {
    companion object {
        val typeToken by lazy { object : TypeToken<List<Double>>() {} }
    }

    override fun get(): List<Double>? {
        val json = SPUtils.getInstance().getString(key)
        return GsonUtils.fromJson<List<Double>>(json, typeToken.type)
    }

    override fun set(value: List<Double>?) {
        super.set(value)
        if (value == null) {
            SPUtils.getInstance().remove(key)
        } else {
            val json = GsonUtils.toJson(value)
            SPUtils.getInstance().put(key, json)
        }
    }
}

@Suppress("UnstableApiUsage")
class BoolListKVImpl(
    private val key: String
) : KVListItem<Boolean>() {
    companion object {
        val typeToken by lazy { object : TypeToken<List<Boolean>>() {} }
    }

    override fun get(): List<Boolean>? {
        val json = SPUtils.getInstance().getString(key)
        return GsonUtils.fromJson<List<Boolean>>(json, typeToken.type)
    }

    override fun set(value: List<Boolean>?) {
        super.set(value)
        if (value == null) {
            SPUtils.getInstance().remove(key)
        } else {
            val json = GsonUtils.toJson(value)
            SPUtils.getInstance().put(key, json)
        }
    }
}

class ObjectListKVImpl<T : Serializable>(
    private val key: String,
    private val clazz: Class<T>
) : KVListItem<T>() {
    override fun get(): List<T>? {
        val json = SPUtils.getInstance().getString(key)
        return GsonUtils.fromJson<List<T>>(json, clazz)
    }

    override fun set(value: List<T>?) {
        super.set(value)

        if (value == null) {
            SPUtils.getInstance().remove(key)
        } else {
            val json = GsonUtils.toJson(value)
            SPUtils.getInstance().put(key, json)
        }
    }
}