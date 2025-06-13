package com.lalilu.common.kv

import android.content.Context
import com.blankj.utilcode.util.Utils
import kotlin.reflect.KClass

object KVSpSaver : KVSaver {
    private val sp by lazy { Utils.getApp().getSharedPreferences("spName", Context.MODE_PRIVATE) }

    override fun <T> readData(
        key: String,
        defaultValue: T?,
        clazz: KClass<*>,
    ): T = with(sp) {
        when {
            defaultValue is Long || clazz == Long::class ->
                getLong(key, (defaultValue as? Long) ?: 0L)

            defaultValue is Int || clazz == Int::class ->
                getInt(key, (defaultValue as? Int) ?: 0)

            defaultValue is Boolean || clazz == Boolean::class ->
                getBoolean(key, (defaultValue as? Boolean) ?: false)

            defaultValue is Float || clazz == Float::class ->
                getFloat(key, (defaultValue as? Float) ?: 0f)

            defaultValue is String || clazz == String::class ->
                getString(key, (defaultValue as? String) ?: "")

            else -> throw UnsupportedOperationException("Not supported type: ${defaultValue}")
        } as T
    }

    override fun <T> saveData(
        key: String,
        value: T?,
        clazz: KClass<*>,
    ) = with(sp.edit()) {
        when (value) {
            null -> {
                remove(key)
                return@with
            }

            is Long -> putLong(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Float -> putFloat(key, value)
            is String -> putString(key, value)
            else -> throw UnsupportedOperationException("Not supported type: $value")
        }.apply()
    }
}