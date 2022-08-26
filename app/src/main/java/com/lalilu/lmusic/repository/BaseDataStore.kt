package com.lalilu.lmusic.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.blankj.utilcode.util.GsonUtils
import com.google.common.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

abstract class BaseDataStore : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    protected abstract val dataStore: DataStore<Preferences>
    protected abstract val Context.dataStore: DataStore<Preferences>

    fun <T : Any> ListPreferenceKey<T>.set(value: List<T>?) = launch { dataStore.set(value) }
    fun <T : Any> ListPreferenceKey<T>.get(): List<T> = dataStore.get()
    fun <T : Any> ListPreferenceKey<T>.listen(): Flow<List<T>> = dataStore.listen()

    fun <T : Any> Preferences.Key<T>.set(value: T?) = launch {
        dataStore.edit {
            if (value == null) {
                it.remove(this@set)
                return@edit
            }
            it[this@set] = value
        }
    }

    fun <T : Any> Preferences.Key<T>.get(): T? = runBlocking {
        listen().first()
    }

    fun <T : Any> Preferences.Key<T>.listen(): Flow<T?> = dataStore.data.map { preferences ->
        preferences[this]
    }

    fun intListPreferencesKey(name: String, temp: Boolean = false): ListPreferenceKey<Int> =
        ListPreferenceKey(name, temp)

    fun longListPreferencesKey(name: String, temp: Boolean = false): ListPreferenceKey<Long> =
        ListPreferenceKey(name, temp)

    fun stringListPreferencesKey(name: String, temp: Boolean = false): ListPreferenceKey<String> =
        ListPreferenceKey(name, temp)

    fun floatListPreferencesKey(name: String, temp: Boolean = false): ListPreferenceKey<String> =
        ListPreferenceKey(name, temp)

    fun boolListPreferencesKey(name: String, temp: Boolean = false): ListPreferenceKey<Boolean> =
        ListPreferenceKey(name, temp)

    class ListPreferenceKey<I : Any> internal constructor(
        name: String,
        private val temp: Boolean = false
    ) {
        private val typeToken = object : TypeToken<List<I>>() {}.type
        private val preferencesKey = stringPreferencesKey(name)
        private var tempList: List<I>? = emptyList()

        suspend fun DataStore<Preferences>.set(value: List<I>?) {
            if (temp) tempList = value
            this.edit { it[preferencesKey] = GsonUtils.toJson(value ?: emptyList<I>()) }
        }

        fun DataStore<Preferences>.get(): List<I> {
            if (temp && tempList != null && tempList!!.isNotEmpty()) {
                return tempList as List<I>
            }
            return runBlocking { listen().first() }
        }

        fun DataStore<Preferences>.listen(): Flow<List<I>> =
            this.data.map { preferences ->
                GsonUtils.fromJson(preferences[preferencesKey], typeToken) ?: emptyList()
            }
    }
}