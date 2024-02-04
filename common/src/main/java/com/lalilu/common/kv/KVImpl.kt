package com.lalilu.common.kv

import io.fastkv.FastKV
import io.fastkv.interfaces.FastEncoder

class IntKVItem(
    val key: String,
    private val fastKV: FastKV
) : KVItem<Int>() {
    override fun get(): Int? {
        return if (!fastKV.contains(key)) null
        else fastKV.getInt(key)
    }

    override fun set(value: Int?) {
        if (value == null) fastKV.remove(key)
        else fastKV.putInt(key, value)
    }
}


class LongKVItem(
    val key: String,
    private val fastKV: FastKV
) : KVItem<Long>() {

    override fun get(): Long? {
        return if (!fastKV.contains(key)) null
        else fastKV.getLong(key)
    }

    override fun set(value: Long?) {
        if (value == null) fastKV.remove(key)
        else fastKV.putLong(key, value)
    }
}

class BooleanKVItem(
    val key: String,
    private val fastKV: FastKV
) : KVItem<Boolean>() {
    override fun get(): Boolean? {
        return if (!fastKV.contains(key)) null
        else fastKV.getBoolean(key)
    }

    override fun set(value: Boolean?) {
        super.set(value)
        if (value == null) fastKV.remove(key)
        else fastKV.putBoolean(key, value)
    }
}

class FloatKVItem(
    val key: String,
    private val fastKV: FastKV
) : KVItem<Float>() {
    override fun get(): Float? {
        return if (!fastKV.contains(key)) null
        else fastKV.getFloat(key)
    }

    override fun set(value: Float?) {
        super.set(value)
        if (value == null) fastKV.remove(key)
        else fastKV.putFloat(key, value)
    }
}


class DoubleKVItem(
    val key: String,
    private val fastKV: FastKV
) : KVItem<Double>() {
    override fun get(): Double? {
        return if (!fastKV.contains(key)) null
        else fastKV.getDouble(key)
    }

    override fun set(value: Double?) {
        super.set(value)
        if (value == null) fastKV.remove(key)
        else fastKV.putDouble(key, value)
    }
}

class StringKVItem(
    val key: String,
    private val fastKV: FastKV
) : KVItem<String>() {
    override fun get(): String? {
        return if (!fastKV.contains(key)) null
        else fastKV.getString(key)
    }

    override fun set(value: String?) {
        super.set(value)
        if (value == null) fastKV.remove(key)
        else fastKV.putString(key, value)
    }
}

class ByteArrayKVItem(
    val key: String,
    private val fastKV: FastKV
) : KVItem<ByteArray>() {
    override fun get(): ByteArray? {
        return if (!fastKV.contains(key)) null
        else fastKV.getArray(key)
    }

    override fun set(value: ByteArray?) {
        super.set(value)
        if (value == null) fastKV.remove(key)
        else fastKV.putArray(key, value)
    }
}

class ObjectKVItem<T>(
    val key: String,
    private val fastKV: FastKV,
    private val encoder: FastEncoder<T>
) : KVItem<T>() {
    override fun get(): T? {
        return if (!fastKV.contains(key)) null
        else fastKV.getObject<T>(key)
    }

    override fun set(value: T?) {
        super.set(value)
        if (value == null) fastKV.remove(key)
        else fastKV.putObject(key, value, encoder)
    }
}


class IntListKVItem(
    val key: String,
    private val fastKV: FastKV
) : KVListItem<Int>(key, fastKV) {
    override fun set(key: String, value: Int?) {
        if (value == null) fastKV.remove(key)
        else fastKV.putInt(key, value)
    }

    override fun get(key: String): Int? {
        return if (!fastKV.contains(key)) null
        else fastKV.getInt(key)
    }
}

class LongListKVItem(
    val key: String,
    private val fastKV: FastKV
) : KVListItem<Long>(key, fastKV) {
    override fun set(key: String, value: Long?) {
        if (value == null) fastKV.remove(key)
        else fastKV.putLong(key, value)
    }

    override fun get(key: String): Long? {
        return if (!fastKV.contains(key)) null
        else fastKV.getLong(key)
    }
}

class BooleanListKVItem(
    val key: String,
    private val fastKV: FastKV
) : KVListItem<Boolean>(key, fastKV) {
    override fun set(key: String, value: Boolean?) {
        if (value == null) fastKV.remove(key)
        else fastKV.putBoolean(key, value)
    }

    override fun get(key: String): Boolean? {
        return if (!fastKV.contains(key)) null
        else fastKV.getBoolean(key)
    }
}

class FloatListKVItem(
    val key: String,
    private val fastKV: FastKV
) : KVListItem<Float>(key, fastKV) {
    override fun set(key: String, value: Float?) {
        if (value == null) fastKV.remove(key)
        else fastKV.putFloat(key, value)
    }

    override fun get(key: String): Float? {
        return if (!fastKV.contains(key)) null
        else fastKV.getFloat(key)
    }
}

class DoubleListKVItem(
    val key: String,
    private val fastKV: FastKV
) : KVListItem<Double>(key, fastKV) {
    override fun set(key: String, value: Double?) {
        if (value == null) fastKV.remove(key)
        else fastKV.putDouble(key, value)
    }

    override fun get(key: String): Double? {
        return if (!fastKV.contains(key)) null
        else fastKV.getDouble(key)
    }
}

class StringListKVItem(
    val key: String,
    private val fastKV: FastKV
) : KVListItem<String>(key, fastKV) {
    override fun set(key: String, value: String?) {
        if (value == null) fastKV.remove(key)
        else fastKV.putString(key, value)
    }

    override fun get(key: String): String? {
        return if (!fastKV.contains(key)) null
        else fastKV.getString(key)
    }
}


class ObjectListKVItem<T>(
    val key: String,
    private val fastKV: FastKV,
    private val encoder: FastEncoder<T>
) : KVListItem<T>(key, fastKV) {
    override fun set(key: String, value: T?) {
        if (value == null) fastKV.remove(key)
        else fastKV.putObject(key, value, encoder)
    }

    override fun get(key: String): T? {
        return if (!fastKV.contains(key)) null
        else fastKV.getObject<T>(key)
    }
}

