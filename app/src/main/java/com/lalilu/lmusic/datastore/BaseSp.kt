package com.lalilu.lmusic.datastore

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import com.blankj.utilcode.util.GsonUtils
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class BaseSp : OnSharedPreferenceChangeListener {
    abstract val sp: SharedPreferences
    private val typeToken: Type by lazy { object : TypeToken<Map<String, Any>>() {}.type }

    fun backup(): String {
        return GsonUtils.toJson(sp.all)
    }

    fun restore(json: String) {
        val map = GsonUtils.fromJson<Map<String, Any>>(json, typeToken)

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

    companion object {
        private val intSpMap = LinkedHashMap<String, SpItem<Int>>()
        private val floatSpMap = LinkedHashMap<String, SpItem<Float>>()
        private val longSpMap = LinkedHashMap<String, SpItem<Long>>()
        private val stringSpMap = LinkedHashMap<String, SpItem<String>>()
        private val boolSpMap = LinkedHashMap<String, SpItem<Boolean>>()
        private val intSpListMap = LinkedHashMap<String, SpListItem<Int>>()
        private val floatSpListMap = LinkedHashMap<String, SpListItem<Float>>()
        private val longSpListMap = LinkedHashMap<String, SpListItem<Long>>()
        private val stringSpListMap = LinkedHashMap<String, SpListItem<String>>()
        private val boolSpListMap = LinkedHashMap<String, SpListItem<Boolean>>()
        private val intSpSetMap = LinkedHashMap<String, SpSetItem<Int>>()
        private val floatSpSetMap = LinkedHashMap<String, SpSetItem<Float>>()
        private val longSpSetMap = LinkedHashMap<String, SpSetItem<Long>>()
        private val stringSpSetMap = LinkedHashMap<String, SpSetItem<String>>()
        private val boolSpSetMap = LinkedHashMap<String, SpSetItem<Boolean>>()
    }

    private val onUpdateMap = LinkedHashMap<String, SpItem<out Any>>()

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        onUpdateMap[key]?.update()
    }

    private fun <T : SpItem<out Any>> T.registerOnUpdateCallback(): T = also {
        onUpdateMap[this.key] = it
        sp.registerOnSharedPreferenceChangeListener(this@BaseSp)
    }

    fun intSp(key: String, defaultValue: Int = -1) =
        intSpMap[key] ?: SpItem(key, defaultValue, sp)
            .also { intSpMap[key] = it }
            .registerOnUpdateCallback()

    fun floatSp(key: String, defaultValue: Float = -1F) =
        floatSpMap[key] ?: SpItem(key, defaultValue, sp)
            .also { floatSpMap[key] = it }
            .registerOnUpdateCallback()

    fun longSp(key: String, defaultValue: Long = -1L) =
        longSpMap[key] ?: SpItem(key, defaultValue, sp)
            .also { longSpMap[key] = it }
            .registerOnUpdateCallback()

    fun stringSp(key: String, defaultValue: String = "") =
        stringSpMap[key] ?: SpItem(key, defaultValue, sp)
            .also { stringSpMap[key] = it }
            .registerOnUpdateCallback()

    fun boolSp(key: String, defaultValue: Boolean = false) =
        boolSpMap[key] ?: SpItem(key, defaultValue, sp)
            .also { boolSpMap[key] = it }
            .registerOnUpdateCallback()

    fun intListSp(key: String, defaultValue: List<Int> = emptyList()) =
        intSpListMap[key] ?: SpListItem(key, defaultValue, sp)
            .also { intSpListMap[key] = it }
            .registerOnUpdateCallback()

    fun floatListSp(key: String, defaultValue: List<Float> = emptyList()) =
        floatSpListMap[key] ?: SpListItem(key, defaultValue, sp)
            .also { floatSpListMap[key] = it }
            .registerOnUpdateCallback()

    fun longListSp(key: String, defaultValue: List<Long> = emptyList()) =
        longSpListMap[key] ?: SpListItem(key, defaultValue, sp)
            .also { longSpListMap[key] = it }
            .registerOnUpdateCallback()

    fun stringListSp(key: String, defaultValue: List<String> = emptyList()) =
        stringSpListMap[key] ?: SpListItem(key, defaultValue, sp)
            .also { stringSpListMap[key] = it }
            .registerOnUpdateCallback()

    fun boolListSp(key: String, defaultValue: List<Boolean> = emptyList()) =
        boolSpListMap[key] ?: SpListItem(key, defaultValue, sp)
            .also { boolSpListMap[key] = it }
            .registerOnUpdateCallback()

    fun intSetSp(key: String, defaultValue: List<Int> = emptyList()) =
        intSpSetMap[key] ?: SpSetItem(key, defaultValue, sp)
            .also { intSpSetMap[key] = it }
            .registerOnUpdateCallback()

    fun floatSetSp(key: String, defaultValue: List<Float> = emptyList()) =
        floatSpSetMap[key] ?: SpSetItem(key, defaultValue, sp)
            .also { floatSpSetMap[key] = it }
            .registerOnUpdateCallback()

    fun longSetSp(key: String, defaultValue: List<Long> = emptyList()) =
        longSpSetMap[key] ?: SpSetItem(key, defaultValue, sp)
            .also { longSpSetMap[key] = it }
            .registerOnUpdateCallback()

    fun stringSetSp(key: String, defaultValue: List<String> = emptyList()) =
        stringSpSetMap[key] ?: SpSetItem(key, defaultValue, sp)
            .also { stringSpSetMap[key] = it }
            .registerOnUpdateCallback()

    fun boolSetSp(key: String, defaultValue: List<Boolean> = emptyList()) =
        boolSpSetMap[key] ?: SpSetItem(key, defaultValue, sp)
            .also { boolSpSetMap[key] = it }
            .registerOnUpdateCallback()
}

@Suppress("UNCHECKED_CAST")
open class SpItem<T : Any> internal constructor(
    val key: String,
    val defaultValue: T,
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
        return sp.getValue(defaultValue::class, key, defaultValue)
    }

    fun set(value: T?) {
        sp.setValue(defaultValue::class, key, value)
    }

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
open class SpListItem<T> internal constructor(
    key: String,
    defaultValue: List<T>,
    sp: SharedPreferences
) : SpItem<List<T>>(key, defaultValue, sp) {
    private val typeToken = object : TypeToken<List<T>>() {}.type

    open fun remove(item: T) {
        get().toMutableList()
            .apply {
                remove(item)
                set(this)
            }
    }

    open fun remove(items: Collection<T>) {
        get().toMutableList()
            .apply {
                removeAll(items)
                set(this)
            }
    }

    open fun add(item: T) {
        get().toMutableList()
            .apply {
                add(item)
                set(this)
            }
    }

    open fun add(items: Collection<T>) {
        get().toMutableList()
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
        defaultValue: Any
    ): T {
        return (GsonUtils.fromJson(getString(key, ""), typeToken) ?: defaultValue) as T
    }
}

class SpSetItem<T> internal constructor(
    key: String,
    defaultValue: List<T>,
    sp: SharedPreferences
) : SpListItem<T>(key, defaultValue, sp) {

    override fun remove(item: T) {
        get().toMutableSet()
            .apply {
                remove(item)
                set(this.toList())
            }
    }

    override fun remove(items: Collection<T>) {
        get().toMutableSet()
            .apply {
                removeAll(items.toSet())
                set(this.toList())
            }
    }

    override fun add(item: T) {
        get().toMutableSet()
            .apply {
                add(item)
                set(this.toList())
            }
    }

    override fun add(items: Collection<T>) {
        get().toMutableSet()
            .apply {
                addAll(items.toSet())
                set(this.toList())
            }
    }
}


