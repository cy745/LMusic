package com.lalilu.lmusic.utils

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.annotation.IdRes
import com.blankj.utilcode.util.Utils

val list: MutableList<SharedPreferences.OnSharedPreferenceChangeListener> = ArrayList()

inline fun <reified T> SharedPreferences.listen(
    @IdRes resId: Int,
    defaultValue: T? = null,
    initialize: Boolean = true,
    crossinline callback: (T) -> Unit = {}
): Boolean {
    val targetKey = try {
        Utils.getApp().resources.getString(resId)
    } catch (e: Resources.NotFoundException) {
        return false
    }

    return listen(targetKey, defaultValue, initialize, callback)
}

inline fun <reified T> SharedPreferences.listen(
    targetKey: String,
    defaultValue: T? = null,
    initialize: Boolean = true,
    crossinline callback: (T) -> Unit = {}
): Boolean {
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
        if (targetKey == key) {
            get(key, defaultValue, callback)
        }
    }
    list.add(listener)

    registerOnSharedPreferenceChangeListener(listener)
    if (initialize) {
        get(targetKey, defaultValue, callback)
    }
    return true
}

inline fun <reified T> SharedPreferences.getByResId(
    @IdRes resId: Int,
    defaultValue: T? = null,
    callback: (T) -> Unit = {}
): T? {
    val targetKey = try {
        Utils.getApp().resources.getString(resId)
    } catch (e: Resources.NotFoundException) {
        return null
    }

    return get(targetKey, defaultValue, callback)
}

inline fun <reified T> SharedPreferences.get(
    key: String,
    defaultValue: T? = null,
    callback: (T) -> Unit = {}
): T? {
    val value = try {
        when (T::class.java) {
            java.lang.String::class.java -> getString(key, defaultValue as String?)
            java.lang.Boolean::class.java -> getBoolean(key, (defaultValue ?: false) as Boolean)
            java.lang.Long::class.java -> getLong(key, (defaultValue ?: Long.MIN_VALUE) as Long)
            java.lang.Integer::class.java -> getInt(key, (defaultValue ?: Int.MIN_VALUE) as Int)
            else -> null
        } as T?
    } catch (e: Exception) {
        return null
    }
    value?.let(callback)
    return value
}
