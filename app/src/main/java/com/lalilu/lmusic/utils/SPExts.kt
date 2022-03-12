package com.lalilu.lmusic.utils

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.annotation.IdRes
import com.blankj.utilcode.util.Utils


inline fun <reified T> SharedPreferences.listen(
    @IdRes resId: Int,
    defaultValue: T? = null,
    crossinline callback: (T) -> Unit = {}
): T? {
    val targetKey = try {
        Utils.getApp().resources.getString(resId)
    } catch (e: Resources.NotFoundException) {
        return null
    }

    val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
        if (targetKey == key) {
            val value = this.get(key, defaultValue)
            value?.let { callback(it) }
        }
    }
    list.add(listener)

    registerOnSharedPreferenceChangeListener(listener)
    val value = this.get(targetKey, defaultValue)
    value?.let { callback(it) }
    return value
}

val list: MutableList<SharedPreferences.OnSharedPreferenceChangeListener> = ArrayList()

inline fun <reified T> SharedPreferences.get(
    key: String,
    defaultValue: T? = null
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
    return value
}
