package com.lalilu.lmusic.manager

import android.content.Context
import android.content.SharedPreferences
import kotlin.collections.set

object SpManager {
    abstract class SpListener<T>(val callback: (T) -> Unit) {
        fun onUpdate(newValue: T) = callback(newValue)
    }

    class SpBoolListener(
        val defaultValue: Boolean = false,
        callback: (Boolean) -> Unit
    ) : SpListener<Boolean>(callback)

    class SpFloatListener(
        val defaultValue: Float = 0f,
        callback: (Float) -> Unit
    ) : SpListener<Float>(callback)

    class SpIntListener(
        val defaultValue: Int = 0,
        callback: (Int) -> Unit
    ) : SpListener<Int>(callback)

    private val onSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
            update(sp, key)
        }

    private var sp: SharedPreferences? = null
    private val listeners: LinkedHashMap<String, SpListener<out Any>> = linkedMapOf()

    fun init(context: Context) {
        sp = context.getSharedPreferences(
            context.applicationContext.packageName, Context.MODE_PRIVATE
        ).also {
            it.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
        }
    }

    fun update(sp: SharedPreferences, key: String) {
        listeners[key]?.let {
            when (it) {
                is SpBoolListener -> it.onUpdate(sp.getBoolean(key, it.defaultValue))
                is SpFloatListener -> it.onUpdate(sp.getFloat(key, it.defaultValue))
                is SpIntListener -> it.onUpdate(sp.getInt(key, it.defaultValue))
            }
        }
    }

    fun <K : Any, T : SpListener<K>> listen(key: String, listener: T) {
        listeners[key] = listener
        sp?.let { update(it, key) }
    }
}