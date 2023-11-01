package com.lalilu.common.base

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import com.blankj.utilcode.util.GsonUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class BaseSp {
    protected abstract fun obtainSourceSp(): SharedPreferences
    private val mapTypeClazz by lazy { mapOf<String, Any>()::class.java }

    val spMap = LinkedHashMap<String, SpItem<out Any>>()
    val sp: SharedPreferences by lazy { obtainSourceSp() }
    private val onChangeCallback = OnSharedPreferenceChangeListener { _, key ->
        spMap[key]?.update()
    }

    init {
        Handler(Looper.getMainLooper()).post {
            sp.registerOnSharedPreferenceChangeListener(onChangeCallback)
        }
    }

    fun backup(): String {
        return GsonUtils.toJson(sp.all)
    }

    fun restore(json: String) {
        val map = GsonUtils.fromJson(json, mapTypeClazz)

        sp.edit {
            for ((key, value) in map) {
                when (value) {
                    is Int -> putInt(key, value)
                    is Float -> putFloat(key, value)
                    is Long -> putLong(key, value)
                    is String -> putString(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> print("Unsupported type: [$key:$value]")
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> obtain(
        key: String,
        defaultValue: T = obtainDefaultValue(),
        autoSave: Boolean = true,
    ): SpItem<T> = spMap.getOrPut(key) { SpItem(key, autoSave, defaultValue, sp) } as SpItem<T>

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> obtainList(
        key: String,
        defaultValue: List<T> = emptyList(),
        autoSave: Boolean = true,
    ): SpListItem<T> =
        spMap.getOrPut(key) { SpListItem.obtain(key, autoSave, defaultValue, sp) } as SpListItem<T>

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> obtainSet(
        key: String,
        defaultValue: List<T> = emptyList(),
        autoSave: Boolean = true,
    ): SpSetItem<T> =
        spMap.getOrPut(key) { SpSetItem.obtain(key, autoSave, defaultValue, sp) } as SpSetItem<T>

    inline fun <reified T> obtainDefaultValue(): T {
        return when (T::class) {
            Int::class -> -1
            Float::class -> -1F
            Long::class -> -1L
            String::class -> ""
            Boolean::class -> false
            else -> throw IllegalArgumentException("[No Matching Type Defined]: ${T::class}")
        } as T
    }
}

@Suppress("UNCHECKED_CAST")
open class SpItem<T : Any>(
    val key: String,
    private val autoSave: Boolean = true,
    private val defaultValue: T,
    private val sp: SharedPreferences
) : MutableState<T> {
    companion object {
        private val keyKeeper = LinkedHashSet<String>()
    }

    private val id = "${sp.hashCode()}-$key"
    private var state: MutableState<T>? = null
    private lateinit var listener: OnSharedPreferenceChangeListener

    init {
        if (keyKeeper.contains(id)) {
            throw IllegalStateException("Mustn't define duplicate key in same sharePreference.")
        } else {
            keyKeeper.add(id)
        }
    }

    override var value: T
        get() {
            state = state ?: mutableStateOf(get())

            return state!!.value
        }
        set(value) {
            state = state ?: mutableStateOf(get())

            val oldValue = state!!.value
            state!!.value = value
            if (oldValue != value && autoSave) {
                set(value)
            }
        }

    override fun component1(): T = this.value
    override fun component2(): (T) -> Unit = { this.value = it }
    operator fun setValue(thisObj: Any?, property: KProperty<*>, v: T) = run { value = v }
    operator fun getValue(thisObj: Any?, property: KProperty<*>): T = this.value

    protected fun get(): T = sp.getValue(defaultValue::class, key, defaultValue)
    protected fun set(value: T?) = sp.setValue(defaultValue::class, key, value)

    fun update() = run { value = get() }
    fun save() = set(value)

    fun flow(requireCurrentValue: Boolean = true): Flow<T?> {
        return callbackFlow {
            val listener = OnSharedPreferenceChangeListener { spParams, keyParam ->
                if (keyParam == key) {
                    val newValue = spParams.getValue(defaultValue::class, keyParam, defaultValue)
                    trySend(newValue)
                }
            }.also {
                if (requireCurrentValue) {
                    val newValue = sp.getValue(defaultValue::class, key, defaultValue)
                    trySend(newValue)
                }
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
        defaultValue: Any
    ): T {
        return when (clazz) {
            Int::class -> getInt(key, defaultValue as Int)
            Float::class -> getFloat(key, defaultValue as Float)
            String::class -> getString(key, defaultValue as String)
            Boolean::class -> getBoolean(key, defaultValue as Boolean)
            Long::class -> getLong(key, defaultValue as Long)
            else -> throw IllegalArgumentException("[No Matching Type Defined]: $clazz $defaultValue")
        } as T
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
open class SpListItem<K : Any>(
    key: String,
    autoSave: Boolean,
    defaultClazz: KClass<K>,
    defaultValue: List<K>,
    sp: SharedPreferences
) : SpItem<List<K>>(key, autoSave, defaultValue, sp) {
    private val mapTypeClazz by lazy { GsonUtils.getListType(defaultClazz.java) }

    open fun remove(item: K) {
        value = value.minus(item)
    }

    open fun remove(items: Collection<K>) {
        value = value.minus(items.toSet())
    }

    open fun add(item: K) {
        value = value.plus(item)
    }

    open fun add(items: Collection<K>) {
        value = value.plus(items)
    }

    override fun <T> SharedPreferences.getValue(
        clazz: KClass<out T & Any>,
        key: String,
        defaultValue: Any
    ): T {
        val string = getString(key, "")?.takeIf { it.isNotBlank() } ?: return defaultValue as T
        return (GsonUtils.fromJson(string, mapTypeClazz) ?: defaultValue) as T
    }

    override fun <T> SharedPreferences.setValue(
        clazz: KClass<out T & Any>,
        key: String,
        value: Any?
    ) {
        val string = GsonUtils.toJson(value).takeIf { it.isNotBlank() }
        edit {
            putString(key, string)
            commit()
        }
    }

    companion object {
        inline fun <reified T : Any> obtain(
            key: String,
            autoSave: Boolean = true,
            defaultValue: List<T> = emptyList(),
            sp: SharedPreferences
        ): SpListItem<T> {
            return SpListItem(
                key = key,
                autoSave = autoSave,
                defaultValue = defaultValue,
                defaultClazz = T::class,
                sp = sp
            )
        }
    }
}

class SpSetItem<T : Any>(
    key: String,
    autoSave: Boolean,
    defaultClazz: KClass<T>,
    defaultValue: List<T>,
    sp: SharedPreferences
) : SpListItem<T>(key, autoSave, defaultClazz, defaultValue, sp) {

    override fun remove(item: T) {
        value = value.toSet().minus(item).toList()
    }

    override fun remove(items: Collection<T>) {
        value = value.toSet().minus(items.toSet()).toList()
    }

    override fun add(item: T) {
        value = value.toSet().plus(item).toList()
    }

    override fun add(items: Collection<T>) {
        value = value.toSet().plus(items).toList()
    }

    companion object {
        inline fun <reified T : Any> obtain(
            key: String,
            autoSave: Boolean = true,
            defaultValue: List<T> = emptyList(),
            sp: SharedPreferences
        ): SpSetItem<T> {
            return SpSetItem(
                key = key,
                autoSave = autoSave,
                defaultValue = defaultValue,
                defaultClazz = T::class,
                sp = sp
            )
        }
    }
}


