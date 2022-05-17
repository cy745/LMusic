package com.lalilu.lmusic.manager

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext

object SpManager : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    abstract class SpListener<T>(
        private val callback: suspend (T) -> Unit,
        private val async: Boolean
    ) {
        fun onUpdate(newValue: T) {
            launch(
                if (async) Dispatchers.IO else Dispatchers.Unconfined
            ) {
                callback(newValue)
            }
        }
    }

    class SpBoolListener(
        val defaultValue: Boolean = false,
        async: Boolean = false,
        callback: suspend (Boolean) -> Unit
    ) : SpListener<Boolean>(callback, async)

    class SpFloatListener(
        val defaultValue: Float = 0f,
        async: Boolean = false,
        callback: suspend (Float) -> Unit
    ) : SpListener<Float>(callback, async)

    class SpIntListener(
        val defaultValue: Int = 0,
        async: Boolean = false,
        callback: suspend (Int) -> Unit
    ) : SpListener<Int>(callback, async)

    class SpStringListener(
        val defaultValue: String = "",
        async: Boolean = false,
        callback: suspend (String) -> Unit
    ) : SpListener<String>(callback, async)

    private var sp: SharedPreferences? = null
    private val listeners: LinkedHashMap<String, SpListener<out Any>> = linkedMapOf()

    fun init(context: Context) {
        sp = context.getSharedPreferences(
            context.applicationContext.packageName, Context.MODE_PRIVATE
        ).also {
            it.registerOnSharedPreferenceChangeListener { sp, key ->
                update(sp, key)
            }
        }
    }

    fun update(sp: SharedPreferences?, key: String) {
        listeners[key]?.let {
            when (it) {
                is SpBoolListener -> it.onUpdate(
                    sp?.getBoolean(key, it.defaultValue) ?: it.defaultValue
                )
                is SpFloatListener -> it.onUpdate(
                    sp?.getFloat(key, it.defaultValue) ?: it.defaultValue
                )
                is SpIntListener -> it.onUpdate(
                    sp?.getInt(key, it.defaultValue) ?: it.defaultValue
                )
                is SpStringListener -> it.onUpdate(
                    sp?.getString(key, it.defaultValue) ?: it.defaultValue
                )
            }
        }
    }

    /**
     * 监听对应key的更新，需要注意一个Key暂时只能对应一个监听器
     */
    fun <K : Any, T : SpListener<K>> listen(
        key: String,
        listener: T,
        immediate: Boolean = true,
    ) {
        listeners[key] = listener
        if (immediate) {
            update(sp, key)
        }
    }
//
//    fun update(key: String, value: Any) {
//        listeners[key]?.let { listener ->
//            when {
//                listener is SpBoolListener && value is Boolean -> {
//                    sp?.edit()?.putBoolean(key, value)?.apply()
//                }
//                listener is SpFloatListener && value is Float -> {
//                    sp?.edit()?.putFloat(key, value)?.apply()
//                }
//                listener is SpIntListener && value is Int -> {
//                    sp?.edit()?.putInt(key, value)?.apply()
//                }
//                listener is SpStringListener && value is String -> {
//                    sp?.edit()?.putString(key, value)?.apply()
//                }
//                else -> {}
//            }
//        }
//    }
}