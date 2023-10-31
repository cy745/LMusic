package com.lalilu.common.base

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class BaseSp {
    protected abstract fun obtainSourceSp(): SharedPreferences

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
        return Json.encodeToString(sp.all)
    }

    fun restore(json: String) {
        val map = Json.decodeFromString<Map<String, Any>>(json)

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
        defaultValue: T = obtainDefaultValue()
    ): SpItem<T> = spMap.getOrPut(key) { SpItem(key, defaultValue, sp) } as SpItem<T>

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> obtainList(
        key: String,
        defaultValue: List<T> = emptyList()
    ): SpListItem<T> =
        spMap.getOrPut(key) { SpListItem.obtain(key, defaultValue, sp) } as SpListItem<T>

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> obtainSet(
        key: String,
        defaultValue: List<T> = emptyList()
    ): SpSetItem<T> =
        spMap.getOrPut(key) { SpSetItem.obtain(key, defaultValue, sp) } as SpSetItem<T>

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
            if (oldValue != value) {
                set(value)
            }
        }

    override fun component1(): T = this.value
    override fun component2(): (T) -> Unit = { this.value = it }

    operator fun setValue(thisObj: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }

    operator fun getValue(thisObj: Any?, property: KProperty<*>): T = this.value

    fun update() {
        this.value = get()
    }

    fun get(): T {
        return getValue(sp, defaultValue::class, key, defaultValue)
    }

    fun set(value: T?) {
        setValue(sp, defaultValue::class, key, value)
    }

    fun flow(requireCurrentValue: Boolean = true): Flow<T?> {
        return callbackFlow {
            val listener = OnSharedPreferenceChangeListener { spParams, keyParam ->
                if (keyParam == key) {
                    val newValue = getValue(spParams, defaultValue::class, keyParam, defaultValue)
                    trySend(newValue)
                }
            }.also {
                if (requireCurrentValue) {
                    val newValue = getValue(sp, defaultValue::class, key, defaultValue)
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
    protected open fun <T : Any> getValue(
        sp: SharedPreferences,
        clazz: KClass<T>,
        key: String,
        defaultValue: Any
    ): T = sp.run {
        return when (clazz) {
            Int::class -> getInt(key, defaultValue as Int)
            Float::class -> getFloat(key, defaultValue as Float)
            String::class -> getString(key, defaultValue as String)
            Boolean::class -> getBoolean(key, defaultValue as Boolean)
            Long::class -> getLong(key, defaultValue as Long)
            else -> throw IllegalArgumentException("[No Matching Type Defined]: $clazz $defaultValue")
        } as T
    }

    protected open fun <T : Any> setValue(
        sp: SharedPreferences,
        clazz: KClass<T>,
        key: String,
        value: Any?
    ) = sp.apply {
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
    val defaultClazz: KClass<K>,
    defaultValue: List<K>,
    sp: SharedPreferences
) : SpItem<List<K>>(key, defaultValue, sp) {

    open fun remove(item: K) {
        set(value.minus(item))
    }

    open fun remove(items: Collection<K>) {
        set(value.minus(items.toSet()))
    }

    open fun add(item: K) {
        set(value.plus(item))
    }

    open fun add(items: Collection<K>) {
        set(value.plus(items))
    }

    override fun <T : Any> getValue(
        sp: SharedPreferences,
        clazz: KClass<T>,
        key: String,
        defaultValue: Any
    ): T {
        val string = sp.getString(key, "")?.takeIf { it.isNotBlank() } ?: return emptyList<K>() as T
        return when (defaultClazz) {
            Int::class -> Json.decodeFromString<List<Int>>(string)
            Float::class -> Json.decodeFromString<List<Float>>(string)
            String::class -> Json.decodeFromString<List<String>>(string)
            Boolean::class -> Json.decodeFromString<List<Boolean>>(string)
            Long::class -> Json.decodeFromString<List<Long>>(string)
            else -> throw IllegalArgumentException("No Matching Type Defined.")
        } as T
    }

    override fun <T : Any> setValue(
        sp: SharedPreferences,
        clazz: KClass<T>,
        key: String,
        value: Any?
    ): SharedPreferences {
        val string = when (defaultClazz) {
            Int::class -> Json.encodeToString(value as List<Int>)
            Float::class -> Json.encodeToString(value as List<Float>)
            String::class -> Json.encodeToString(value as List<String>)
            Boolean::class -> Json.encodeToString(value as List<Boolean>)
            Long::class -> Json.encodeToString(value as List<Long>)
            else -> throw IllegalArgumentException("No Matching Type Defined.")
        }.takeIf { it.isNotBlank() }

        return super.setValue(sp, String::class, key, string)
    }

    companion object {
        inline fun <reified T : Any> obtain(
            key: String,
            defaultValue: List<T> = emptyList(),
            sp: SharedPreferences
        ): SpListItem<T> {
            return SpListItem(
                key = key,
                defaultValue = defaultValue,
                defaultClazz = T::class,
                sp = sp
            )
        }
    }
}

class SpSetItem<T : Any>(
    key: String,
    defaultClazz: KClass<T>,
    defaultValue: List<T>,
    sp: SharedPreferences
) : SpListItem<T>(key, defaultClazz, defaultValue, sp) {

    override fun remove(item: T) {
        set(value.toSet().minus(item).toList())
    }

    override fun remove(items: Collection<T>) {
        set(value.toSet().minus(items.toSet()).toList())
    }

    override fun add(item: T) {
        set(value.toSet().plus(item).toList())
    }

    override fun add(items: Collection<T>) {
        set(value.toSet().plus(items).toList())
    }

    companion object {
        inline fun <reified T : Any> obtain(
            key: String,
            defaultValue: List<T> = emptyList(),
            sp: SharedPreferences
        ): SpSetItem<T> {
            return SpSetItem(
                key = key,
                defaultValue = defaultValue,
                defaultClazz = T::class,
                sp = sp
            )
        }
    }
}


