package com.lalilu.lmusic.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.funny.data_saver.core.DataSaverInterface
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class DataSaverDataStorePreferences : DataSaverInterface {
    companion object {
        private lateinit var dataStore: DataStore<Preferences>
        fun setDataStorePreferences(dataStore: DataStore<Preferences>) {
            this.dataStore = dataStore
        }
    }

    override fun <T> readData(key: String, default: T): T {
        return runBlocking { get(dataStore, key, default) }
    }

    override fun <T> saveData(key: String, data: T) {
        runBlocking { put(dataStore, key, data) }
    }

    // Reference：https://blog.csdn.net/qq_36707431/article/details/119447093
    private suspend fun <T> get(dataStore: DataStore<Preferences>, key: String, default: T): T {
        return when (default!!::class) {
            Int::class -> {
                dataStore.data.map { setting ->
                    setting[intPreferencesKey(key)] ?: default
                }.first() as T
            }
            Long::class -> {
                dataStore.data.map { setting ->
                    setting[longPreferencesKey(key)] ?: default
                }.first() as T
            }
            Double::class -> {
                dataStore.data.map { setting ->
                    setting[doublePreferencesKey(key)] ?: default
                }.first() as T
            }
            Float::class -> {
                dataStore.data.map { setting ->
                    setting[floatPreferencesKey(key)] ?: default
                }.first() as T
            }
            Boolean::class -> {
                dataStore.data.map { setting ->
                    setting[booleanPreferencesKey(key)] ?: default
                }.first() as T
            }
            String::class -> {
                dataStore.data.map { setting ->
                    setting[stringPreferencesKey(key)] ?: default
                }.first() as T
            }
            else -> {
                throw IllegalArgumentException("This type can be get into DataStore")
            }
        }
    }

    private suspend fun <T> put(dataStore: DataStore<Preferences>, key: String, value: T) {
        dataStore.edit { setting ->
            when (value) {
                is Int -> setting[intPreferencesKey(key)] = value
                is Long -> setting[longPreferencesKey(key)] = value
                is Double -> setting[doublePreferencesKey(key)] = value
                is Float -> setting[floatPreferencesKey(key)] = value
                is Boolean -> setting[booleanPreferencesKey(key)] = value
                is String -> setting[stringPreferencesKey(key)] = value
                else -> throw IllegalArgumentException("This type can be saved into DataStore")
            }
        }
    }


}