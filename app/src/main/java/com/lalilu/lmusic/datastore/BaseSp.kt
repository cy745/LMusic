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
}

@Suppress("UNCHECKED_CAST")
open class SpItem<T>(
    private val key: String,
    private val defaultValue: T,
    private val sp: SharedPreferences,
) {
    companion object {
        private val keyKeeper = LinkedHashSet<String>()
    }

    private val id = "${sp.hashCode()}-$key"

    init {
        if (keyKeeper.contains(id)) {
            throw IllegalStateException("Mustn't define duplicate key in same sharePreference.")
        } else {
            keyKeeper.add(id)
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
            }.also { sp.registerOnSharedPreferenceChangeListener(it) }

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
class SpListItem<T>(
    key: String,
    defaultValue: List<T>,
    sp: SharedPreferences
) : SpItem<List<T>>(key, defaultValue, sp) {
    private val typeToken = object : TypeToken<List<T>>() {}.type

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



