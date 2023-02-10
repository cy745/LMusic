package com.lalilu.lmusic.datastore

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.core.content.edit
import com.blankj.utilcode.util.GsonUtils
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.reflect.KClass

abstract class BaseSp {
    abstract val sp: SharedPreferences

    fun intSp(key: String, defaultValue: Int = -1) = SpItem(key, defaultValue, sp)
    fun floatSp(key: String, defaultValue: Float = -1F) = SpItem(key, defaultValue, sp)
    fun longSp(key: String, defaultValue: Long = -1L) = SpItem(key, defaultValue, sp)
    fun stringSp(key: String, defaultValue: String = "") = SpItem(key, defaultValue, sp)
    fun boolSp(key: String, defaultValue: Boolean = false) = SpItem(key, defaultValue, sp)

    fun intListSp(key: String, defaultValue: List<Int> = emptyList()) =
        SpListItem(key, defaultValue, sp)

    fun floatListSp(key: String, defaultValue: List<Float> = emptyList()) =
        SpListItem(key, defaultValue, sp)

    fun longListSp(key: String, defaultValue: List<Long> = emptyList()) =
        SpListItem(key, defaultValue, sp)

    fun stringListSp(key: String, defaultValue: List<String> = emptyList()) =
        SpListItem(key, defaultValue, sp)

    fun boolListSp(key: String, defaultValue: List<Boolean> = emptyList()) =
        SpListItem(key, defaultValue, sp)

    fun intSetSp(key: String, defaultValue: List<Int> = emptyList()) =
        SpSetItem(key, defaultValue, sp)

    fun floatSetSp(key: String, defaultValue: List<Float> = emptyList()) =
        SpSetItem(key, defaultValue, sp)

    fun longSetSp(key: String, defaultValue: List<Long> = emptyList()) =
        SpSetItem(key, defaultValue, sp)

    fun stringSetSp(key: String, defaultValue: List<String> = emptyList()) =
        SpSetItem(key, defaultValue, sp)

    fun boolSetSp(key: String, defaultValue: List<Boolean> = emptyList()) =
        SpSetItem(key, defaultValue, sp)
}

@Suppress("UNCHECKED_CAST")
open class SpItem<T>(
    private val key: String,
    private val defaultValue: T,
    private val sp: SharedPreferences,
    checkUnique: Boolean = false
) {
    companion object {
        private val keyKeeper = LinkedHashSet<String>()
    }

    private val id = "${sp.hashCode()}-$key"

    init {
        if (checkUnique) {
            if (keyKeeper.contains(id)) {
                throw IllegalStateException("Mustn't define duplicate key in same sharePreference.")
            } else {
                keyKeeper.add(id)
            }
        }
    }

    fun get(): T? {
        return sp.getValue(defaultValue!!::class, key, defaultValue)
    }

    fun set(value: T?) {
        println("setValue: $key $value")
        sp.setValue(defaultValue!!::class, key, value)
    }

    fun flow(): Flow<T?> {
        return callbackFlow {
            val listener = OnSharedPreferenceChangeListener { spParams, keyParam ->
                if (keyParam == key) {
                    trySend(
                        spParams.getValue(
                            this@SpItem::defaultValue::class, keyParam, defaultValue
                        ) as T?
                    )
                }
            }.also {
                trySend(
                    sp.getValue(this@SpItem::defaultValue::class, key, defaultValue) as T?
                )
                sp.registerOnSharedPreferenceChangeListener(it)
            }

            awaitClose {
                sp.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    open fun <T> SharedPreferences.getValue(
        clazz: KClass<out T & Any>,
        key: String,
        defaultValue: Any?
    ): T? {
        return when (clazz) {
            Int::class -> getInt(key, defaultValue as Int)
            Float::class -> getFloat(key, defaultValue as Float)
            String::class -> getString(key, defaultValue as String)
            Boolean::class -> getBoolean(key, defaultValue as Boolean)
            Long::class -> getLong(key, defaultValue as Long)
            else -> throw IllegalArgumentException("No Matching Type Defined.")
        } as T?
    }

    open fun <T> SharedPreferences.setValue(clazz: KClass<out T & Any>, key: String, value: Any?) {
        edit {
            when (clazz) {
                Int::class -> putInt(key, value?.let { it as Int } ?: -1)
                Float::class -> putFloat(key, value?.let { it as Float } ?: -1f)
                String::class -> putString(key, value as? String)
                Boolean::class -> putBoolean(key, value?.let { it as Boolean } ?: false)
                Long::class -> putLong(key, value?.let { it as Long } ?: -1L)
                else -> throw IllegalArgumentException("No Matching Type Defined.")
            }
            commit()
        }
    }
}

@Suppress("UNCHECKED_CAST")
open class SpListItem<T>(
    key: String,
    defaultValue: List<T>,
    sp: SharedPreferences
) : SpItem<List<T>>(key, defaultValue, sp) {
    private val typeToken = object : TypeToken<List<T>>() {}.type

    open fun remove(item: T) {
        (get() ?: emptyList())
            .toMutableList()
            .apply {
                remove(item)
                set(this)
            }
    }

    open fun remove(items: Collection<T>) {
        (get() ?: emptyList())
            .toMutableList()
            .apply {
                removeAll(items)
                set(this)
            }
    }

    open fun add(item: T) {
        (get() ?: emptyList())
            .toMutableList()
            .apply {
                add(item)
                set(this)
            }
    }

    open fun add(items: Collection<T>) {
        (get() ?: emptyList())
            .toMutableList()
            .apply {
                addAll(items)
                set(this)
            }
    }

    override fun <T> SharedPreferences.setValue(
        clazz: KClass<out T & Any>,
        key: String,
        value: Any?
    ) {
        edit {
            putString(key, GsonUtils.toJson(value, typeToken))
            commit()
        }
    }

    override fun <T> SharedPreferences.getValue(
        clazz: KClass<out T & Any>,
        key: String,
        defaultValue: Any?
    ): T? {
        return (GsonUtils.fromJson(getString(key, ""), typeToken) ?: defaultValue) as T?
    }
}

class SpSetItem<T>(
    key: String,
    defaultValue: List<T>,
    sp: SharedPreferences
) : SpListItem<T>(key, defaultValue, sp) {

    override fun remove(item: T) {
        (get() ?: emptyList())
            .toMutableSet()
            .apply {
                remove(item)
                set(this.toList())
            }
    }

    override fun remove(items: Collection<T>) {
        (get() ?: emptyList())
            .toMutableSet()
            .apply {
                removeAll(items.toSet())
                set(this.toList())
            }
    }

    override fun add(item: T) {
        (get() ?: emptyList())
            .toMutableSet()
            .apply {
                add(item)
                set(this.toList())
            }
    }

    override fun add(items: Collection<T>) {
        (get() ?: emptyList())
            .toMutableSet()
            .apply {
                addAll(items.toSet())
                set(this.toList())
            }
    }
}


